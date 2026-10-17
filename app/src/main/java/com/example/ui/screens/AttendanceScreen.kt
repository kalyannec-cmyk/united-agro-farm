package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceRecord
import com.example.model.AttendanceStatus
import com.example.model.Employee
import com.example.model.EmployeeMonthlySummary
import com.example.ui.theme.AgroGoldTertiary
import com.example.ui.theme.AgroGreenPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    employees: List<Employee>,
    dailyAttendance: List<AttendanceRecord>,
    monthlyAttendance: List<AttendanceRecord>,
    selectedDate: String,
    selectedMonth: String,
    isEnglish: Boolean = false,
    onDateChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onMarkAttendance: (Employee, AttendanceStatus, String, String) -> Unit,
    onMarkAllPresent: (String) -> Unit,
    onAddEmployee: (String, String, String, Double, Double) -> Unit,
    onUpdateEmployee: (Employee) -> Unit,
    onDeleteEmployee: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }

    val activeEmployees = employees.filter { it.isActive }
    val attendanceMap = dailyAttendance.associateBy { it.employeeId }

    val presentCount = dailyAttendance.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = dailyAttendance.count { it.status == AttendanceStatus.ABSENT }
    val halfDayCount = dailyAttendance.count { it.status == AttendanceStatus.HALF_DAY }
    val leaveCount = dailyAttendance.count { it.status == AttendanceStatus.PAID_LEAVE }
    val overtimeCount = dailyAttendance.count { it.status == AttendanceStatus.OVERTIME }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("attendance_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Selection Header
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AgroGreenPrimary,
                modifier = Modifier.testTag("attendance_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (isEnglish) "Day Attendance" else "दैनिक हाजिरी",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_daily_attendance")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (isEnglish) "Monthly Summary" else "मासिक विवरण",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_monthly_summary")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = if (isEnglish) "Staff Roster" else "कर्मचारी सूची",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_staff_roster")
                )
            }

            when (selectedTab) {
                0 -> {
                    // DAY-WISE ATTENDANCE TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // Date selector card
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                onDateChange(shiftDate(selectedDate, -1))
                                            },
                                            modifier = Modifier.testTag("btn_prev_date")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Previous Day",
                                                tint = AgroGreenPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                // Reset to today
                                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                                onDateChange(today)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = AgroGreenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = selectedDate,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B3B22)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                onDateChange(shiftDate(selectedDate, 1))
                                            },
                                            modifier = Modifier.testTag("btn_next_date")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Day",
                                                tint = AgroGreenPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick Stats Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        AttendanceCountChip(label = if (isEnglish) "Staff" else "कुल", count = activeEmployees.size, color = Color.DarkGray)
                                        AttendanceCountChip(label = if (isEnglish) "Present" else "उपस्थित", count = presentCount, color = Color(0xFF2E7D32))
                                        AttendanceCountChip(label = if (isEnglish) "Absent" else "अनुपस्थित", count = absentCount, color = Color(0xFFD32F2F))
                                        AttendanceCountChip(label = if (isEnglish) "Half-Day" else "आधा दिन", count = halfDayCount, color = Color(0xFFF57C00))
                                        AttendanceCountChip(label = if (isEnglish) "Leave" else "बिदा", count = leaveCount, color = Color(0xFF1976D2))
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { onMarkAllPresent(selectedDate) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AgroGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_mark_all_present")
                                    ) {
                                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isEnglish) "Mark All Active Staff Present (Today)" else "सबै सक्रिय कर्मचारी उपस्थित चिन्हित गर्नुहोस्",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Employee daily attendance list
                        if (activeEmployees.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEnglish) "No active employees found. Add staff in 'Staff Roster' tab." else "कुनै कर्मचारी फेला परेन। कर्मचारी सूचीबाट नयाँ थप्नुहोस्।",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            items(activeEmployees, key = { it.id }) { employee ->
                                val record = attendanceMap[employee.id]
                                val currentStatus = record?.status ?: AttendanceStatus.ABSENT

                                DailyEmployeeAttendanceCard(
                                    employee = employee,
                                    currentStatus = currentStatus,
                                    record = record,
                                    isEnglish = isEnglish,
                                    onStatusSelected = { newStatus ->
                                        onMarkAttendance(employee, newStatus, record?.checkInTime ?: "07:00 AM", record?.remarks ?: "")
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // MONTHLY ATTENDANCE SUMMARY TAB
                    val monthlySummaries = computeMonthlySummaries(
                        employees = activeEmployees,
                        records = monthlyAttendance,
                        monthYear = selectedMonth
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        item {
                            // Month Selector Card
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onMonthChange(shiftMonth(selectedMonth, -1)) },
                                            modifier = Modifier.testTag("btn_prev_month")
                                        ) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month", tint = AgroGreenPrimary)
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (isEnglish) "Month: $selectedMonth" else "महिना: $selectedMonth",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B3B22)
                                            )
                                            Text(
                                                text = if (isEnglish) "Lalitpur United Agro Farm Payroll" else "ललितपुर युनाइटेड एग्रो फार्म तलब विवरण",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }

                                        IconButton(
                                            onClick = { onMonthChange(shiftMonth(selectedMonth, 1)) },
                                            modifier = Modifier.testTag("btn_next_month")
                                        ) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = AgroGreenPrimary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val totalEstimatedPay = monthlySummaries.sumOf { it.calculatedPay }
                                    val avgAttendanceRate = if (monthlySummaries.isNotEmpty()) {
                                        monthlySummaries.map { it.attendancePercentage }.average()
                                    } else 0.0

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (isEnglish) "Est. Total Wages" else "अनुमानित कुल तलब",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF2E7D32)
                                                )
                                                Text(
                                                    text = "रु. ${totalEstimatedPay.toInt()}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1B5E20)
                                                )
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (isEnglish) "Farm Avg. Attendance" else "औसत फार्म हाजिरी दर",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFF57F17)
                                                )
                                                Text(
                                                    text = "%.1f%%".format(avgAttendanceRate),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Employee monthly breakdowns
                        items(monthlySummaries) { summary ->
                            MonthlyEmployeeSummaryCard(summary = summary, isEnglish = isEnglish)
                        }
                    }
                }

                2 -> {
                    // STAFF ROSTER TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isEnglish) "Farm Staff Members" else "फार्मका कार्यरत कर्मचारीहरू",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isEnglish) "${employees.size} registered (${activeEmployees.size} active)" else "कुल ${employees.size} दर्ता (${activeEmployees.size} सक्रिय)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Button(
                                        onClick = { showAddEmployeeDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_open_add_employee")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = if (isEnglish) "Add Staff" else "कर्मचारी थप्नुहोस्")
                                    }
                                }
                            }
                        }

                        items(employees, key = { it.id }) { emp ->
                            StaffRosterCard(
                                employee = emp,
                                isEnglish = isEnglish,
                                onEdit = { employeeToEdit = emp },
                                onDelete = { onDeleteEmployee(emp.id) },
                                onToggleActive = { updatedActive ->
                                    onUpdateEmployee(emp.copy(isActive = updatedActive))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add Employee FAB on Roster Tab
        if (selectedTab == 2) {
            FloatingActionButton(
                onClick = { showAddEmployeeDialog = true },
                containerColor = AgroGoldTertiary,
                contentColor = Color(0xFF1B3B22),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fab_add_employee")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    }

    if (showAddEmployeeDialog) {
        AddOrEditEmployeeDialog(
            isEnglish = isEnglish,
            onDismiss = { showAddEmployeeDialog = false },
            onSubmit = { name, role, phone, wage, salary ->
                onAddEmployee(name, role, phone, wage, salary)
                showAddEmployeeDialog = false
            }
        )
    }

    employeeToEdit?.let { emp ->
        AddOrEditEmployeeDialog(
            existing = emp,
            isEnglish = isEnglish,
            onDismiss = { employeeToEdit = null },
            onSubmit = { name, role, phone, wage, salary ->
                onUpdateEmployee(
                    emp.copy(
                        name = name,
                        role = role,
                        phone = phone,
                        dailyWage = wage,
                        monthlySalary = salary
                    )
                )
                employeeToEdit = null
            }
        )
    }
}

@Composable
fun AttendanceCountChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun DailyEmployeeAttendanceCard(
    employee: Employee,
    currentStatus: AttendanceStatus,
    record: AttendanceRecord?,
    isEnglish: Boolean,
    onStatusSelected: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("attendance_card_${employee.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = employee.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B22),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = employee.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32)
                    )
                }

                Surface(
                    color = Color(currentStatus.colorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(currentStatus.colorHex))
                ) {
                    Text(
                        text = if (isEnglish) currentStatus.labelEn else currentStatus.labelNp,
                        color = Color(currentStatus.colorHex),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status selection action chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusSelectButton(
                    label = if (isEnglish) "Present" else "उपस्थित",
                    isSelected = currentStatus == AttendanceStatus.PRESENT,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.PRESENT) }
                )
                StatusSelectButton(
                    label = if (isEnglish) "Absent" else "अनुपस्थित",
                    isSelected = currentStatus == AttendanceStatus.ABSENT,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.ABSENT) }
                )
                StatusSelectButton(
                    label = if (isEnglish) "Half-Day" else "आधा दिन",
                    isSelected = currentStatus == AttendanceStatus.HALF_DAY,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.HALF_DAY) }
                )
                StatusSelectButton(
                    label = if (isEnglish) "Leave" else "बिदा",
                    isSelected = currentStatus == AttendanceStatus.PAID_LEAVE,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.PAID_LEAVE) }
                )
            }

            if (record != null && record.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "कैफियत: ${record.remarks}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusSelectButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) color else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color),
        modifier = modifier
            .clickable { onClick() }
            .testTag("status_btn_$label")
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun MonthlyEmployeeSummaryCard(summary: EmployeeMonthlySummary, isEnglish: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.employee.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B22)
                    )
                    Text(
                        text = summary.employee.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "रु. ${summary.calculatedPay.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = if (isEnglish) "Payable Amount" else "पाउने तलब",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance rate progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Attendance: %.1f%%".format(summary.attendancePercentage) else "हाजिरी दर: %.1f%%".format(summary.attendancePercentage),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${summary.presentCount} उपस्थित / ${summary.absentCount} अनुपस्थित / ${summary.halfDayCount} आधा / ${summary.leaveCount} बिदा",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (summary.attendancePercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = AgroGreenPrimary,
                trackColor = Color(0xFFE0E0E0),
            )
        }
    }
}

@Composable
fun StaffRosterCard(
    employee: Employee,
    isEnglish: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("staff_card_${employee.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B3B22)
                )
                Text(
                    text = employee.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroGreenPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = employee.phone.ifBlank { "सम्पर्क छैन" }, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "दैनिक: रु. ${employee.dailyWage.toInt()} | मासिक: रु. ${employee.monthlySalary.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = employee.isActive,
                    onCheckedChange = { onToggleActive(it) }
                )
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = AgroGreenPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
fun AddOrEditEmployeeDialog(
    existing: Employee? = null,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var role by remember { mutableStateOf(existing?.role ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var dailyWage by remember { mutableStateOf(existing?.dailyWage?.toString() ?: "850") }
    var monthlySalary by remember { mutableStateOf(existing?.monthlySalary?.toString() ?: "25500") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) {
                    if (isEnglish) "Add New Employee" else "नयाँ कर्मचारी दर्ता"
                } else {
                    if (isEnglish) "Edit Employee Details" else "कर्मचारी विवरण सम्पादन"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isEnglish) "Employee Full Name" else "कर्मचारीको पूरा नाम") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_employee_name")
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text(if (isEnglish) "Role / Department (e.g. Dairy, Poultry)" else "पद / जिम्मेवारी (गाईगोठ, कुखुरा, तरकारी)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_employee_role")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isEnglish) "Contact Phone" else "सम्पर्क फोन नम्बर") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_employee_phone")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dailyWage,
                        onValueChange = { dailyWage = it },
                        label = { Text(if (isEnglish) "Daily Wage (रु.)" else "दैनिक ज्याला") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_daily_wage")
                    )
                    OutlinedTextField(
                        value = monthlySalary,
                        onValueChange = { monthlySalary = it },
                        label = { Text(if (isEnglish) "Monthly Salary (रु.)" else "मासिक तलब") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_monthly_salary")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && role.isNotBlank()) {
                        onSubmit(
                            name.trim(),
                            role.trim(),
                            phone.trim(),
                            dailyWage.toDoubleOrNull() ?: 800.0,
                            monthlySalary.toDoubleOrNull() ?: 24000.0
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                modifier = Modifier.testTag("btn_save_employee")
            ) {
                Text(text = if (isEnglish) "Save Staff" else "सुरक्षित गर्नुहोस्")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isEnglish) "Cancel" else "रद्द गर्नुहोस्")
            }
        }
    )
}

fun shiftDate(dateStr: String, deltaDays: Int): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, deltaDays)
        sdf.format(cal.time)
    } catch (_: Exception) {
        dateStr
    }
}

fun shiftMonth(monthStr: String, deltaMonths: Int): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdf.parse(monthStr) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, deltaMonths)
        sdf.format(cal.time)
    } catch (_: Exception) {
        monthStr
    }
}

fun computeMonthlySummaries(
    employees: List<Employee>,
    records: List<AttendanceRecord>,
    monthYear: String
): List<EmployeeMonthlySummary> {
    val totalDaysInMonth = 30 // standard agricultural payroll month
    val grouped = records.groupBy { it.employeeId }

    return employees.map { emp ->
        val empRecords = grouped[emp.id] ?: emptyList()
        val present = empRecords.count { it.status == AttendanceStatus.PRESENT }
        val halfDay = empRecords.count { it.status == AttendanceStatus.HALF_DAY }
        val absent = empRecords.count { it.status == AttendanceStatus.ABSENT }
        val leave = empRecords.count { it.status == AttendanceStatus.PAID_LEAVE }
        val overtime = empRecords.count { it.status == AttendanceStatus.OVERTIME }

        // Calculation: (Present days * dailyWage) + (halfDay * dailyWage * 0.5) + (leave * dailyWage) + (overtime * dailyWage * 1.5)
        val calculatedWage = (present * emp.dailyWage) +
                (halfDay * emp.dailyWage * 0.5) +
                (leave * emp.dailyWage) +
                (overtime * emp.dailyWage * 1.5)

        val totalPay = if (calculatedWage > 0) calculatedWage else emp.monthlySalary

        EmployeeMonthlySummary(
            employee = emp,
            monthYear = monthYear,
            totalWorkingDays = totalDaysInMonth,
            presentCount = present,
            halfDayCount = halfDay,
            absentCount = absent,
            leaveCount = leave,
            overtimeCount = overtime,
            calculatedPay = totalPay
        )
    }
}
