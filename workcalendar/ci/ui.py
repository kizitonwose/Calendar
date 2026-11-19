#!/usr/bin/env python3
"""Tiny UI automation helper for the emulator smoke test (uiautomator + adb)."""
import base64
import os
import re
import shlex
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

OUT = os.environ.get("SMOKE_OUT", "smoke")
PKG = "io.github.ceniorpomidor.workcalendar"


def adb(*args, check=False):
    return subprocess.run(["adb", *args], capture_output=True, text=True, check=check)


def dump_xml():
    root = None
    for _ in range(5):
        adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
        res = adb("shell", "cat", "/sdcard/ui.xml")
        if "<hierarchy" in res.stdout:
            root = ET.fromstring(res.stdout[res.stdout.index("<?xml") if "<?xml" in res.stdout else res.stdout.index("<hierarchy"):])
            if not dismiss_system_dialog(root):
                return root
        time.sleep(1.5)
    return root


def dismiss_system_dialog(root):
    """Closes "<app> isn't responding" dialogs of other apps that a slow emulator shows over the app."""
    if not any("isn't responding" in (n.get("text") or "") or "не отвечает" in (n.get("text") or "") for n in nodes(root)):
        return False
    for label in ("Wait", "Подождать", "Close app", "Закрыть приложение"):
        node = find(root, label, exact=True)
        if node is not None:
            x, y = center(node)
            adb("shell", "input", "tap", str(x), str(y))
            print(f">> dismissed system dialog ({label})")
            return True
    return False


def nodes(root):
    for n in root.iter("node"):
        yield n


def center(node):
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
    if not m:
        return None
    x1, y1, x2, y2 = map(int, m.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def find(root, text, exact=False):
    best = None
    for n in nodes(root):
        for attr in ("text", "content-desc"):
            value = n.get(attr) or ""
            if (value == text) if exact else (text in value):
                if best is None or len(value) < len(best[1]):
                    best = (n, value)
    return best[0] if best else None


def texts(root):
    out = []
    for n in nodes(root):
        t = (n.get("text") or "").strip()
        d = (n.get("content-desc") or "").strip()
        if t or d:
            c = center(n) or (0, 0)
            out.append((c[1], c[0], t or f"[{d}]"))
    out.sort()
    return [t for _, _, t in out]


def crashed():
    log = adb("logcat", "-d", "-b", "crash").stdout
    if "FATAL EXCEPTION" in log or ("AndroidRuntime" in log and PKG in log):
        print("=====CRASH=====")
        print(log[-6000:])
        return True
    return False


def screenshot(name, inline):
    os.makedirs(OUT, exist_ok=True)
    png = os.path.join(OUT, f"{name}.png")
    with open(png, "wb") as f:
        f.write(subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True).stdout)
    if inline:
        jpg = os.path.join(OUT, f"{name}.jpg")
        r = subprocess.run(["convert", png, "-resize", "432x", "-quality", "60", jpg], capture_output=True)
        if r.returncode == 0 and os.path.exists(jpg):
            data = base64.b64encode(open(jpg, "rb").read()).decode()
            print(f"=====SCREENSHOT {name} {len(data)}=====")
            for i in range(0, len(data), 4000):
                print(data[i:i + 4000])
            print(f"=====END {name}=====")


def cmd_frame(png, name):
    """Prints a small inline copy of a screenshot taken by the test script."""
    jpg = png[:-4] + ".jpg"
    r = subprocess.run(["convert", png, "-resize", "270x", "-quality", "55", jpg], capture_output=True)
    if r.returncode == 0 and os.path.exists(jpg):
        data = base64.b64encode(open(jpg, "rb").read()).decode()
        print(f"=====SCREENSHOT {name} {len(data)}=====")
        for i in range(0, len(data), 4000):
            print(data[i:i + 4000])
        print(f"=====END {name}=====")


def has_text(lines, expect):
    """[expect] is a part of a text on the screen; with a leading "=" the whole text."""
    return any(t == expect[1:] for t in lines) if expect.startswith("=") else any(expect in t for t in lines)


def cmd_dump(name, inline="0", *expects):
    """Prints the texts of the screen; fails when one of [expects] is not on it."""
    time.sleep(1.5)
    root = dump_xml()
    print(f"===== SCREEN {name} =====")
    lines = texts(root) if root is not None else []
    if root is None:
        print("(no ui dump)")
    for t in lines:
        print("  " + t.replace("\n", " / "))
    screenshot(name, inline == "1")
    if crashed():
        sys.exit(1)
    missing = [e for e in expects if e and not has_text(lines, e)]
    if missing:
        print(f"!! expected text not found on {name}: {', '.join(missing)}")
        log = adb("logcat", "-d").stdout.splitlines()
        print("\n".join(line for line in log if "WorkCalendar" in line or " E " in line)[-6000:])
        sys.exit(1)


def cmd_wait(name, expect, timeout="60"):
    """Waits until [expect] appears on the screen (e.g. a screen opened by the system), then dumps it."""
    deadline = time.time() + int(timeout)
    while time.time() < deadline:
        root = dump_xml()
        if root is not None and has_text(texts(root), expect):
            cmd_dump(name, "1", expect)
            return
        time.sleep(2)
    print(f"!! {expect} did not appear in {timeout} s")
    cmd_dump(name, "1")
    sys.exit(4)


def keyboard_shown():
    out = adb("shell", "dumpsys", "input_method").stdout
    return "mInputShown=true" in out or "isInputViewShown=true" in out


def cmd_hide_keyboard():
    if keyboard_shown():
        adb("shell", "input", "keyevent", "4")
        print(">> hide keyboard")
        time.sleep(1)


def swipe_up():
    size = adb("shell", "wm", "size").stdout
    m = re.search(r"(\d+)x(\d+)", size)
    w, h = (int(m.group(1)), int(m.group(2))) if m else (1080, 2400)
    adb("shell", "input", "swipe", str(w // 2), str(h * 7 // 10), str(w // 2), str(h * 3 // 10), "400")
    time.sleep(1)


def cmd_tap(text, exact="0", scroll="1", attempts=None):
    cmd_hide_keyboard()
    for attempt in range(int(attempts) if attempts else (6 if scroll == "1" else 3)):
        root = dump_xml()
        node = find(root, text, exact == "1") if root is not None else None
        if node is not None:
            x, y = center(node)
            adb("shell", "input", "tap", str(x), str(y))
            print(f">> tap '{text}' at {x},{y}")
            time.sleep(1.2)
            return
        if scroll == "1":
            swipe_up()
        else:
            time.sleep(1.5)
    print(f"!! element not found: {text}")


def cmd_tap_until(text, expect, attempts="4"):
    """Taps [text] until [expect] is on the screen: a slow emulator can lose a tap."""
    for _ in range(int(attempts)):
        root = dump_xml()
        if root is not None and has_text(texts(root), expect):
            print(f">> shown: {expect}")
            return
        cmd_tap(text, "1", "0")
        # Let the next page appear before looking again, so that one tap is not counted twice.
        time.sleep(3)
    root = dump_xml()
    if root is not None and has_text(texts(root), expect):
        print(f">> shown: {expect}")
        return
    print(f"!! {expect} did not appear after tapping {text}")
    sys.exit(5)


def device_zone():
    return adb("shell", "getprop", "persist.sys.timezone").stdout.strip() or "UTC"


def cmd_set_time(local_iso):
    """Moves the emulator clock (needs adb root); the system then broadcasts TIME_SET."""
    from datetime import datetime
    from zoneinfo import ZoneInfo

    when = datetime.fromisoformat(local_iso).replace(tzinfo=ZoneInfo(device_zone()))
    millis = int(when.timestamp() * 1000)
    adb("shell", "settings", "put", "global", "auto_time", "0")
    res = adb("shell", "cmd", "alarm", "set-time", str(millis))
    out = (res.stdout + res.stderr).strip()
    if res.returncode != 0 or "Exception" in out:
        res = adb("shell", "date", f"@{millis // 1000}")
        out += " | date: " + (res.stdout + res.stderr).strip()
    print(f">> set time {local_iso} ({device_zone()}): {out}; device time now {adb('shell', 'date').stdout.strip()}")


def notifications():
    """Titles and texts of the app's notifications that are currently shown."""
    out = adb("shell", "dumpsys", "notification", "--noredact").stdout
    result, current = [], None
    for line in out.splitlines():
        s = line.strip()
        if s.startswith("NotificationRecord("):
            if current:
                result.append(current)
            current = {} if f"pkg={PKG}" in s else None
            continue
        if current is not None:
            m = re.match(r"android\.(title|text|bigText)=\S+ \((.*)\)$", s)
            if m and m.group(1) not in current:
                current[m.group(1)] = m.group(2)
    if current:
        result.append(current)
    unique = []
    for n in result:
        if n and n not in unique:
            unique.append(n)
    return unique


def cmd_notifications(expect=None, timeout="30"):
    """Prints the app's notifications; with [expect] waits until one of them contains the text."""
    deadline = time.time() + int(timeout)
    while True:
        items = notifications()
        found = expect is None or any(expect in v for n in items for v in n.values())
        if found or time.time() > deadline:
            break
        time.sleep(1)
    print("===== NOTIFICATIONS =====")
    for n in items:
        print(f"  {n.get('title', '')} | {n.get('bigText') or n.get('text', '')}")
    if expect is not None:
        print(("OK notification: " if found else "!! notification not found: ") + expect)
        if not found:
            sys.exit(2)


def cmd_notification_action(action, title):
    """Taps an action button of the app's notification in the expanded shade."""
    for _ in range(4):
        root = dump_xml()
        node = find(root, action) if root is not None else None
        if node is not None:
            x, y = center(node)
            adb("shell", "input", "tap", str(x), str(y))
            print(f">> tap notification action '{action}' at {x},{y}")
            time.sleep(1.5)
            return
        header = find(root, title) if root is not None else None
        if header is not None:
            # A collapsed notification is expanded by dragging it down.
            x, y = center(header)
            adb("shell", "input", "swipe", str(x), str(y), str(x), str(y + 600), "400")
            print(">> expand notification")
        time.sleep(1.5)
    print(f"!! notification action not found: {action}")
    sys.exit(3)


def cmd_advance_date(nominal):
    """Find the late-month payout marker on the displayed calendar."""
    from datetime import date, datetime
    month = date.fromisoformat(nominal)
    for _ in range(5):
        root = dump_xml()
        for node in nodes(root) if root is not None else ():
            match = re.search(r"День выплаты, (\d{2}\.\d{2}\.\d{4})", node.get("content-desc", ""))
            if match:
                day = datetime.strptime(match[1], "%d.%m.%Y").date()
                if (day.year, day.month) == (month.year, month.month) and day.day > 15:
                    print(day.isoformat())
                    return
        time.sleep(1)
    sys.exit("Advance payday marker not found")


def cmd_send_reply():
    root = dump_xml()
    for node in nodes(root) if root is not None else ():
        if node.get("resource-id", "").endswith("/remote_input_send"):
            x, y = center(node)
            adb("shell", "input", "tap", str(x), str(y))
            return
    adb("shell", "input", "keyevent", "66")


def cmd_type(text):
    adb("shell", "input", "text", text.replace(" ", "%s"))
    time.sleep(0.5)


def cmd_show(text, attempts="6"):
    """Scrolls down, then up (from the bottom part, where the day panel is) until [text] is visible."""
    size = adb("shell", "wm", "size").stdout
    m = re.search(r"(\d+)x(\d+)", size)
    w, h = (int(m.group(1)), int(m.group(2))) if m else (1080, 2400)
    for down in (True, False):
        for _ in range(int(attempts)):
            root = dump_xml()
            if root is not None and has_text(texts(root), text):
                print(f">> visible: {text}")
                return
            start, end = (h * 88 // 100, h * 50 // 100) if down else (h * 50 // 100, h * 88 // 100)
            adb("shell", "input", "swipe", str(w // 2), str(start), str(w // 2), str(end), "500")
            time.sleep(1.2)
    print(f"!! not visible after scrolling: {text}")


def cmd_route(route):
    # The command line is run by the device shell: quote the route ("&" would end the command).
    adb("shell", f"am start -n {PKG}/.MainActivity -a android.intent.action.VIEW --es route {shlex.quote(route)}")
    print(f">> route {route}")
    time.sleep(2)


def cmd_back():
    adb("shell", "input", "keyevent", "4")
    time.sleep(1)


if __name__ == "__main__":
    command, *args = sys.argv[1:]
    {
        "dump": cmd_dump,
        "wait": cmd_wait,
        "show": cmd_show,
        "tap": cmd_tap,
        "tap-until": cmd_tap_until,
        "type": cmd_type,
        "send-reply": cmd_send_reply,
        "advance-date": cmd_advance_date,
        "route": cmd_route,
        "back": cmd_back,
        "hide-keyboard": cmd_hide_keyboard,
        "frame": cmd_frame,
        "set-time": cmd_set_time,
        "notifications": cmd_notifications,
        "notification-action": cmd_notification_action,
    }[command](*args)
