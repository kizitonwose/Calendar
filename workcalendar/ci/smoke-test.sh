#!/usr/bin/env bash
# Installs the APK on the emulator, walks through the main screens and fails on crashes.
# Prints UI texts of every screen and small inline screenshots of key screens to the log.
set -u
APK=$(ls "$1"/*.apk | head -1)
export PKG=io.github.ceniorpomidor.workcalendar.preview
UI="python3 workcalendar/ci/ui.py"
export SMOKE_OUT=smoke
mkdir -p smoke

adb install -r "$APK" || { echo "install failed"; exit 1; }
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS || true
adb logcat -c
adb shell am start -W -n $PKG/io.github.ceniorpomidor.workcalendar.MainActivity
sleep 8
$UI dump onboarding-1 1 || exit 1
# A slow emulator can lose a tap (or show "isn't responding" over the app): tap until the page changes.
$UI tap-until "Далее" "Почасовая ставка" || exit 1
$UI dump onboarding-rate 0 || exit 1
$UI tap "Ставка в час"
$UI type 350
$UI hide-keyboard
$UI tap-until "Далее" "График работы" || exit 1
$UI dump onboarding-schedule 1 "График работы" || exit 1
$UI tap-until "Далее" "Аванс и зарплата" || exit 1
$UI dump onboarding-payouts 0 "Аванс и зарплата" || exit 1
$UI tap-until "Далее" "Готово" || exit 1
$UI dump onboarding-last 0 || exit 1
$UI tap-until "Готово" "Финансы" || exit 1
sleep 5
# The wizard must be finished: the bottom navigation is visible.
$UI dump calendar 1 "Финансы" || exit 1

# Month swipes: the title follows the swipe and the calendar does not stop between two months.
month_title() {
  python3 -c "import datetime,sys
names = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь']
t = datetime.date.today()
m = t.month - 1 + int(sys.argv[1])
print(names[m % 12], t.year + m // 12)" "$1"
}
# Title of today in the day panel: the selection stays on today while months are swiped.
today_title() {
  python3 -c "import datetime
names = ['января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря']
t = datetime.date.today()
print(t.day, names[t.month - 1])"
}
# Next working day of the 5/2 schedule: holidays and transferred days off have no shifts.
next_weekday() {
  python3 -c "import datetime,sys
fixed = {(1, d) for d in range(1, 9)} | {(2, 23), (3, 8), (5, 1), (5, 9), (6, 12), (11, 4)}
transfers = {'2026-01-09', '2026-03-09', '2026-05-11', '2026-12-31', '2027-02-22', '2027-05-03', '2027-05-10',
             '2027-06-14', '2027-11-05', '2027-12-31'}
d = datetime.date.fromisoformat(sys.argv[1]) + datetime.timedelta(days=1)
while d.weekday() >= 5 or (d.month, d.day) in fixed or d.isoformat() in transfers:
    d += datetime.timedelta(days=1)
print(d)" "$1"
}
# `input swipe` waits until the app has handled each event: while the app is still busy after the
# setup wizard (generating the schedule), a quick swipe can turn into a plain touch without moves.
# Let it settle first and log how long the swipe command takes.
sleep 4
timed_swipe() {
  local start end
  start=$(date +%s%N)
  adb shell input swipe "$@"
  end=$(date +%s%N)
  echo ">> swipe $* took $(( (end - start) / 1000000 )) ms"
}
timed_swipe 950 1000 150 1000 300
if ! $UI dump swipe-fast 0 "$(month_title 1)"; then
  echo "!! WARNING: the first swipe did not change the month; trying once more"
  # Which events the system delivered to the app: shows whether the moves of the swipe arrived.
  adb shell dumpsys input | sed -n '/RecentQueue/,/PendingEvent/p' | cut -c1-240 | head -30
  timed_swipe 950 1000 150 1000 300
  $UI dump swipe-fast-retry 1 "$(month_title 1)" || exit 1
fi
# A slow drag over 60% of the width, slightly diagonal.
adb shell input swipe 950 950 300 1150 900
$UI dump swipe-slow 1 "$(month_title 2)" "$(today_title)" || exit 1
adb shell input swipe 150 1000 950 1000 250
$UI dump swipe-back 0 "$(month_title 1)" || exit 1
$UI tap "Сегодня"
$UI dump swipe-today 0 "$(month_title 0)" || exit 1

YESTERDAY=$(date -d yesterday +%F)
$UI route "shift/new?date=$YESTERDAY&extra=false"
$UI dump shift-new 1 || exit 1
$UI tap "Сохранить" 1
sleep 2
$UI route "unconfirmed"
$UI dump unconfirmed 1 || exit 1
$UI tap "Полностью" 1
sleep 2
$UI dump unconfirmed-after 0 || exit 1
$UI route "day/$YESTERDAY"
$UI dump calendar-day 1 || exit 1

$UI tap "Финансы" 1
sleep 2
$UI dump finance 1 || exit 1
# Payouts of the month: calculated and received amounts, what is still owed; enter the salary that came.
$UI show "Ещё должны выплатить"
$UI dump finance-payouts 1 "Рассчитано" "Пришло" "Ещё должны выплатить" || exit 1
$UI tap "Зарплата" 1
$UI dump received-dialog 0 "Сколько пришло" || exit 1
$UI tap "Сохранить" 1
sleep 2
$UI show "Ещё должны выплатить"
$UI dump finance-received 0 " · $(date +%d.%m)" || exit 1
$UI show "Прогноз выплат"
$UI dump finance-forecast 1 "Прогноз выплат" || exit 1
$UI route "finance/stats"
$UI dump stats 1 || exit 1
$UI route "finance/history"
$UI dump history 0 || exit 1

$UI tap "Настройки" 1
$UI route "settings"
$UI dump settings 1 "Найти настройку" || exit 1
$UI tap "Найти настройку" || exit 1
$UI type "csv"
$UI hide-keyboard
$UI dump settings-search 1 "Резервная копия и экспорт" || exit 1
$UI tap "Очистить поиск" || exit 1
$UI route "settings/templates"
$UI dump templates 1 || exit 1
$UI route "settings/rates"
$UI dump rates 0 || exit 1
$UI route "settings/payouts"
$UI dump payouts 0 || exit 1
VACATION=$(date -d '+20 days' +%F)
$UI route "absence/new?type=VACATION&start=$VACATION"
sleep 2
$UI dump absence 1 || exit 1
# The button is under a long form: allow more scrolling.
$UI tap "Сохранить" 1 1 12
sleep 2
$UI route "day/$VACATION"
$UI dump vacation-day 1 "Отпуск" || exit 1
$UI route "settings/backup"
$UI dump backup 0 || exit 1
$UI route "settings/notifications"
$UI dump notifications 0 || exit 1
# Update from the app: the build under test is the newest version (GitHub may limit requests: not fatal).
$UI route "settings/update"
$UI tap "Проверить обновления" 1
if ! $UI wait update-check "Установлена последняя версия" 40; then
  echo "!! WARNING: the update check did not answer"
fi
$UI route "search"
$UI dump search 0 || exit 1

# ---- Appearance: another palette and the alarm in the bottom bar ----
$UI route "settings/display"
$UI dump appearance 1 "Цветовая палитра" || exit 1
$UI tap "Зелёный" 1
$UI tap "Календарь" 1 || exit 1
$UI tap "Подробный" 1 || exit 1
$UI dump calendar-options 1 "Время смен в ячейках" || exit 1
$UI tap "Компактный" 1 || exit 1
$UI tap "Меню" 1 || exit 1
$UI tap "Будильник" 1
sleep 2
$UI dump appearance-after 0 || exit 1
$UI route "calendar"
$UI dump calendar-green 1 "=Будильник" || exit 1

# Switch each calendar skin through Settings and verify the month is still interactive.
for theme in "Синие ленты" "Пастель" "Комикс" "Граффити" "Офисные стикеры" "Текущее оформление"; do
  $UI route "settings/display"
  $UI tap "Календарь" 1 || exit 1
  $UI tap "Тема календаря ·" 0 1 15 || exit 1
  if [ "$theme" = "Синие ленты" ]; then
    $UI dump theme-picker 0 "Тема календаря" "Офисные стикеры" || exit 1
  fi
  $UI tap-until "$theme" "Тема календаря · $theme" || exit 1
  $UI route "calendar"
  $UI tap "Сегодня" 1 || exit 1
  $UI dump "theme-$theme" 0 "Изменить" || exit 1
done

# ---- Alarms: before every shift, one more at 06:30 on working days, one on a day off, test ring ----
$UI tap "Будильник" 1
$UI dump alarm 0 "Будильников нет" || exit 1
$UI tap "Перед каждой сменой" 1
sleep 2
$UI tap "Добавить" 1
$UI dump alarm-new 0 "Новый будильник" || exit 1
$UI tap "Сохранить" 1
sleep 2
# Rings are listed by time, two per working day: when the second is visible, so is the first.
$UI show "07:30 ·"
$UI dump alarm-on 1 "06:30 ·" "07:30 ·" || exit 1
SATURDAY=$(python3 -c "import datetime
d = datetime.date.today() + datetime.timedelta(days=1)
while d.weekday() != 5:
    d += datetime.timedelta(days=1)
print(d)")
$UI route "day/$SATURDAY"
# The day panel has two buttons; "Изменить" lists what can be changed for the day.
$UI tap "Изменить" 1
$UI dump day-edit 1 "Новый график с этого дня" || exit 1
$UI tap "Не заведён" 1
$UI tap "Будильник на этот день"
$UI dump alarm-day-new 0 "Новый будильник" || exit 1
$UI tap "Сохранить" 1
sleep 4
$UI dump day-alarm 1 "07:00" "Изменить" "Отметить" || exit 1
$UI route "alarm"
$UI tap "Проверить будильник"
$UI wait alarm-test "Отложить на 10 мин" 30 || exit 1
$UI notifications "Проверка будильника" 10 || exit 1
$UI tap "Выключить" 1
sleep 2
$UI show "Ближайшие звонки"
$UI dump alarm-after-test 0 "Ближайшие звонки" || exit 1

# ---- Schedule: stop it from a working day, then apply a new one from the same day ----
CUT=$(next_weekday "$(date -d '+44 days' +%F)")
CUT_RU=$(python3 -c "import datetime,sys; print(datetime.date.fromisoformat(sys.argv[1]).strftime('%d.%m.%Y'))" "$CUT")
$UI route "day/$CUT"
$UI dump cut-day 0 "09:00–18:00" || exit 1
$UI tap "Изменить" 1
$UI tap "Удалить смены с этого дня"
$UI dump cut-confirm 0 "Удалить смены с $CUT_RU?" || exit 1
$UI tap "Удалить" 1
sleep 3
$UI dump cut-done 1 "Выходной" || exit 1
$UI tap "Изменить" 1
$UI tap "Новый график с этого дня"
$UI dump new-schedule 0 "Новый график с $CUT_RU" || exit 1
$UI tap "Применить" 1
$UI dump apply-from 0 "$CUT_RU" || exit 1
$UI tap "Применить" 1
sleep 3
$UI route "day/$CUT"
$UI dump cut-restored 1 "09:00–18:00" || exit 1

# ---- Notifications: shift end -> quick actions (needs adb root to move the clock) ----
echo "===== ALARMS ====="
adb shell dumpsys alarm | grep -A2 "Alarm{.*$PKG" | grep -E "Alarm\{|origWhen=" | head -20
adb root > /dev/null 2>&1 || true
sleep 3
adb wait-for-device
TODAY=$(adb shell date +%F | tr -d '\r')
D1=$(next_weekday "$TODAY")
D2=$(next_weekday "$D1")
adb shell input keyevent 3
$UI set-time "${D1}T18:05:00"
if [ "$(adb shell date +%F | tr -d '\r')" = "$D1" ]; then
  # The shift of D1 (09:00-18:00) has just ended: the notification must appear by itself.
  $UI notifications "Смена завершена" || exit 1
  adb shell cmd statusbar expand-notifications
  sleep 2
  $UI dump shade-shift-end 1 || exit 1
  $UI notification-action "Полностью" "Смена завершена" || exit 1
  $UI notifications "Часы сохранены" 10 || exit 1
  adb shell cmd statusbar collapse
  # Remind twelve hours before the next shift ends, then enter hours in the shade.
  $UI route "settings/notifications"
  $UI tap "Меньше" 1 || exit 1
  $UI dump signed-offset 0 "=-5" || exit 1
  $UI tap "Смещение, мин" 1 || exit 1
  adb shell input keycombination 113 29
  $UI type "-720"
  $UI hide-keyboard
  $UI dump signed-offset-limit 0 "=-720" || exit 1
  adb shell input keyevent 3
  $UI set-time "${D2}T06:00:10"
  $UI notifications "Отметьте часы смены" || exit 1
  adb shell cmd statusbar expand-notifications
  sleep 2
  if $UI notification-action "Ввести часы" "Отметьте часы смены"; then
    $UI type "7,5"
    $UI dump shade-reply 1 || exit 1
    adb shell input keyevent 66
    $UI notifications "7,5" 10 || echo "!! inline reply was not confirmed"
  fi
  adb shell cmd statusbar collapse
  $UI route "day/$D2"
  $UI dump day-after-reply 1 || exit 1

  # ---- Both alarms of the next working day ring by themselves over the lock screen ----
  D3=$(next_weekday "$D2")
  adb shell appops set $PKG USE_FULL_SCREEN_INTENT allow || true
  adb shell input keyevent 223
  $UI set-time "${D3}T06:29:30"
  $UI wait alarm-ring-first "Будильник 06:30" 90 || exit 1
  $UI tap "Выключить" 1
  adb shell input keyevent 223
  $UI set-time "${D3}T07:29:30"
  $UI wait alarm-ring "Будильник 07:30" 90 || exit 1
  $UI tap "Отложить на 10 мин" 1
  $UI notifications "Будильник отложен до 07:4" 15 || exit 1
  adb shell input keyevent 223
  $UI set-time "${D3}T07:39:30"
  $UI wait alarm-snoozed-ring "Будильник 07:4" 150 || exit 1
  $UI tap "Выключить" 1
  # The app's alarms are not system alarm clocks: the clock app keeps the status bar icon,
  # the lock screen and the quick settings tile (the emulator has no clock alarms).
  echo "===== NEXT ALARM CLOCK ====="
  adb shell dumpsys alarm | grep -i -A3 "next alarm clock" | head -12
  if adb shell dumpsys alarm | grep -A2 -i "next alarm clock information" | grep -q "user:0"; then
    echo "!! the app has set a system alarm clock"
    exit 1
  fi
  # The next month's advance has a full period of shifts. Read the actual (possibly moved)
  # payday from the calendar. Record it in the shade, then edit that same payment from its date.
  NOMINAL=$(date -d "${D3:0:7}-01 +1 month +24 days" +%F)
  $UI route "day/$NOMINAL"
  PAYDAY=$($UI advance-date "$NOMINAL") || exit 1
  adb shell input keyevent 3
  $UI set-time "${PAYDAY}T10:01:00"
  $UI notifications "Аванс сегодня" 45 || exit 1
  adb shell cmd statusbar expand-notifications
  $UI notification-action "Внести сумму" "Аванс сегодня" || exit 1
  $UI type "12500,50"
  $UI send-reply || exit 1
  $UI notifications "Выплата получена" 10 || exit 1
  adb shell cmd statusbar collapse
  $UI route "day/$PAYDAY"
  PAYDAY_RU=$(date -d "$PAYDAY" +%d.%m.%Y)
  $UI tap "День выплаты, $PAYDAY_RU" 1 || exit 1
  $UI dump calendar-receipt 1 "Фактическая сумма" "12500,50" || exit 1
  $UI tap "Фактическая сумма" || exit 1
  adb shell input keycombination 113 29
  $UI type "13000,75"
  $UI hide-keyboard
  $UI tap "Сохранить" 1 || exit 1
  $UI wait calendar-receipt-saved "Изменить" 20 || exit 1
  $UI tap "День выплаты, $PAYDAY_RU" 1 || exit 1
  $UI dump calendar-receipt-edited 0 "13000,75" || exit 1
else
  echo "!! the emulator clock could not be changed, notification flow skipped"
fi

echo "===== LOGCAT (app) ====="
adb logcat -d | grep -E "WorkCalendar|AndroidRuntime|FATAL" | tail -80
if adb logcat -d -b crash | grep -q "FATAL EXCEPTION"; then
  echo "App crashed"
  exit 1
fi
# Guard against a script that silently skipped its steps: the key screens must have been visited.
for screen in swipe-slow update-check finance-forecast day-edit cut-restored alarm-test day-after-reply alarm-snoozed-ring; do
  if [ ! -f "smoke/$screen.png" ]; then
    echo "!! step was not reached: $screen"
    exit 1
  fi
done
echo "SMOKE TEST PASSED"
