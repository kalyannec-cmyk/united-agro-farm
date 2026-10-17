const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("orders").get());
});

test("Unauthenticated user: cannot write products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("products").doc("p1").set({
      nameNp: "गाएको दूध",
      category: "DAIRY",
      retailPrice: 110,
      unit: "LITRE",
      stockQuantity: 50,
      userId: "unknown",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can read products", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("products").get());
});

test("Authenticated user: can create a valid order", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_1").set({
      userId: ALICE_UID,
      customerName: "Alice Farmer",
      customerPhone: "9841000000",
      totalAmount: 1100,
      status: "PENDING_VERIFICATION",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot read another user's order", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("bob_ord").set({
      userId: BOB_UID,
      customerName: "Bob",
      customerPhone: "9841999999",
      totalAmount: 500,
      status: "PENDING_VERIFICATION",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("orders").doc("bob_ord").get());
});

test("Authenticated user: can query their own orders matching userId filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("alice_ord").set({
      userId: ALICE_UID,
      customerName: "Alice",
      customerPhone: "9841000000",
      totalAmount: 850,
      status: "VERIFIED",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").where("userId", "==", ALICE_UID).get()
  );
});
