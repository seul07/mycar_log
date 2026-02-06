// Calendar functionality
let currentDate = new Date();
let currentYear = currentDate.getFullYear();
let currentMonth = currentDate.getMonth();
let showYearMonthPicker = false;

function initCalendar() {
    if (!hasCarData) return;
    renderCalendar();
}

function changeMonth(delta) {
    currentMonth += delta;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    } else if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    renderCalendar();
    loadMonthlyExpenses();
}

function renderCalendar() {
    const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];

    const currentMonthEl = document.getElementById('currentMonth');
    currentMonthEl.textContent = `${currentYear}년 ${monthNames[currentMonth]}`;
    currentMonthEl.style.cursor = 'pointer';
    currentMonthEl.onclick = toggleYearMonthPicker;

    const firstDay = new Date(currentYear, currentMonth, 1);
    const lastDay = new Date(currentYear, currentMonth + 1, 0);
    const startDay = firstDay.getDay();
    const totalDays = lastDay.getDate();

    const prevLastDay = new Date(currentYear, currentMonth, 0).getDate();

    const calendarDays = document.getElementById('calendarDays');
    calendarDays.innerHTML = '';

    const today = new Date();
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

    // Previous month days
    for (let i = startDay - 1; i >= 0; i--) {
        const day = prevLastDay - i;
        const dayEl = createDayElement(day, true);
        calendarDays.appendChild(dayEl);
    }

    // Current month days
    for (let i = 1; i <= totalDays; i++) {
        const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
        const dayOfWeek = new Date(currentYear, currentMonth, i).getDay();
        const isToday = dateStr === todayStr;
        const dayEl = createDayElement(i, false, dayOfWeek, isToday, dateStr);
        calendarDays.appendChild(dayEl);
    }

    // Next month days
    const remainingDays = 42 - (startDay + totalDays);
    for (let i = 1; i <= remainingDays; i++) {
        const dayEl = createDayElement(i, true);
        calendarDays.appendChild(dayEl);
    }
}

function createDayElement(day, isOtherMonth, dayOfWeek = 0, isToday = false, dateStr = '') {
    const dayEl = document.createElement('div');
    dayEl.className = 'calendar-day';

    if (isOtherMonth) {
        dayEl.classList.add('other-month');
    } else {
        if (dayOfWeek === 0) dayEl.classList.add('sunday');
        if (dayOfWeek === 6) dayEl.classList.add('saturday');
        if (isToday) dayEl.classList.add('today');

        dayEl.onclick = () => goToDay(dateStr);
    }

    const dayNum = document.createElement('span');
    dayNum.className = 'day-number';
    dayNum.textContent = day;
    dayEl.appendChild(dayNum);

    // Add expense icons if available - fix: access expenseData.expenses
    const expenses = expenseData && expenseData.expenses ? expenseData.expenses : expenseData;
    if (!isOtherMonth && expenses && expenses[dateStr]) {
        const icons = document.createElement('div');
        icons.className = 'day-icons';
        icons.innerHTML = getExpenseIcons(expenses[dateStr]);
        dayEl.appendChild(icons);
    }

    return dayEl;
}

function getExpenseIcons(categories) {
    const iconMap = {
        'MILEAGE': '&#128663;',
        'FUEL': '&#9981;',
        'MAINTENANCE': '&#128295;',
        'TAX': '&#128176;',
        'INSURANCE': '&#128737;',
        'PARKING': '&#127359;',
        'CAR_WASH': '&#128166;',
        'OTHER': '&#128221;'
    };

    const maxVisible = 5; // Show up to 5 icons
    const hasMore = categories.length > maxVisible;

    const iconsToShow = hasMore ? categories.slice(0, maxVisible) : categories;
    const icons = iconsToShow.map(cat =>
        `<span>${iconMap[cat] || ''}</span>`
    );

    if (hasMore) {
        icons.push(`<span class="more-indicator">..</span>`);
    }

    return icons.join('');
}

function goToDay(dateStr) {
    window.location.href = `/expense/${dateStr}`;
}

// Year/Month Picker
function toggleYearMonthPicker() {
    const existingPicker = document.getElementById('yearMonthPicker');
    if (existingPicker) {
        existingPicker.remove();
        return;
    }
    showYearMonthPickerModal();
}

function showYearMonthPickerModal() {
    const picker = document.createElement('div');
    picker.id = 'yearMonthPicker';
    picker.className = 'year-month-picker';

    const thisYear = new Date().getFullYear();
    const years = [];
    for (let y = thisYear - 10; y <= thisYear + 5; y++) {
        years.push(y);
    }

    picker.innerHTML = `
        <div class="picker-overlay" onclick="closeYearMonthPicker()"></div>
        <div class="picker-content">
            <div class="picker-header">
                <button type="button" class="picker-nav" onclick="changePickerYear(-1)">&lt;</button>
                <span id="pickerYear">${currentYear}년</span>
                <button type="button" class="picker-nav" onclick="changePickerYear(1)">&gt;</button>
            </div>
            <div class="picker-months">
                ${[1,2,3,4,5,6,7,8,9,10,11,12].map(m =>
                    `<button type="button" class="picker-month ${m - 1 === currentMonth ? 'active' : ''}"
                             onclick="selectMonth(${m - 1})">${m}월</button>`
                ).join('')}
            </div>
        </div>
    `;

    document.body.appendChild(picker);
}

let pickerYear = currentYear;

function changePickerYear(delta) {
    pickerYear += delta;
    document.getElementById('pickerYear').textContent = `${pickerYear}년`;
}

function selectMonth(month) {
    currentYear = pickerYear;
    currentMonth = month;
    closeYearMonthPicker();
    renderCalendar();
    loadMonthlyExpenses();
}

function closeYearMonthPicker() {
    const picker = document.getElementById('yearMonthPicker');
    if (picker) {
        picker.remove();
    }
    pickerYear = currentYear;
}

// Load expenses for current month via AJAX
function loadMonthlyExpenses() {
    fetch(`/calendar/${currentYear}/${currentMonth + 1}`)
        .then(response => response.json())
        .then(data => {
            expenseData = data;
            renderCalendar();
        })
        .catch(err => console.error('Failed to load expenses:', err));
}

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', function() {
    if (typeof hasCarData !== 'undefined' && hasCarData) {
        pickerYear = currentYear;
        initCalendar();
    }
});

// Form validation helper
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return true;

    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.classList.add('error');
            isValid = false;
        } else {
            field.classList.remove('error');
        }
    });

    return isValid;
}

// Number formatting
function formatNumber(num) {
    return new Intl.NumberFormat('ko-KR').format(num);
}

function formatCurrency(num) {
    return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
    }).format(num);
}
