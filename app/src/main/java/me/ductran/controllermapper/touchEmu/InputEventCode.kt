package me.ductran.controllermapper.touchEmu

// converted from https://elixir.bootlin.com/linux/v4.7/source/include/uapi/linux/input-event-codes.h

// I added
const val DOWN = 1
const val UP = 0

//class InputEventCode {
//    companion object {
        const val INPUT_PROP_POINTER = 0x00    /* needs a pointer */
        const val INPUT_PROP_DIRECT = 0x01    /* direct input devices */
        const val INPUT_PROP_BUTTONPAD = 0x02    /* has button(s) under pad */
        const val INPUT_PROP_SEMI_MT = 0x03    /* touch rectangle only */
        const val INPUT_PROP_TOPBUTTONPAD = 0x04    /* softbuttons at top of pad */
        const val INPUT_PROP_POINTING_STICK = 0x05    /* is a pointing stick */
        const val INPUT_PROP_ACCELEROMETER = 0x06    /* has accelerometer */

        const val INPUT_PROP_MAX = 0x1f
        const val INPUT_PROP_CNT = (INPUT_PROP_MAX + 1)

        /*
     * Event types
     */

        const val EV_SYN = 0x00
        const val EV_KEY = 0x01
        const val EV_REL = 0x02
        const val EV_ABS = 0x03
        const val EV_MSC = 0x04
        const val EV_SW = 0x05
        const val EV_LED = 0x11
        const val EV_SND = 0x12
        const val EV_REP = 0x14
        const val EV_FF = 0x15
        const val EV_PWR = 0x16
        const val EV_FF_STATUS = 0x17
        const val EV_MAX = 0x1f
        const val EV_CNT = (EV_MAX + 1)

        /*
     * Synchronization events.
     */

        const val SYN_REPORT = 0
        const val SYN_CONFIG = 1
        const val SYN_MT_REPORT = 2
        const val SYN_DROPPED = 3
        const val SYN_MAX = 0xf
        const val SYN_CNT = (SYN_MAX + 1)

        /*
     * Keys and buttons
     *
     * Most of the keys/buttons are modeled after USB HUT 1.12
     * (see http://www.usb.org/developers/hidpage).
     * Abbreviations in the comments:
     * AC - Application Control
     * AL - Application Launch Button
     * SC - System Control
     */

        const val KEY_RESERVED = 0
        const val KEY_ESC = 1
        const val KEY_1 = 2
        const val KEY_2 = 3
        const val KEY_3 = 4
        const val KEY_4 = 5
        const val KEY_5 = 6
        const val KEY_6 = 7
        const val KEY_7 = 8
        const val KEY_8 = 9
        const val KEY_9 = 10
        const val KEY_0 = 11
        const val KEY_MINUS = 12
        const val KEY_EQUAL = 13
        const val KEY_BACKSPACE = 14
        const val KEY_TAB = 15
        const val KEY_Q = 16
        const val KEY_W = 17
        const val KEY_E = 18
        const val KEY_R = 19
        const val KEY_T = 20
        const val KEY_Y = 21
        const val KEY_U = 22
        const val KEY_I = 23
        const val KEY_O = 24
        const val KEY_P = 25
        const val KEY_LEFTBRACE = 26
        const val KEY_RIGHTBRACE = 27
        const val KEY_ENTER = 28
        const val KEY_LEFTCTRL = 29
        const val KEY_A = 30
        const val KEY_S = 31
        const val KEY_D = 32
        const val KEY_F = 33
        const val KEY_G = 34
        const val KEY_H = 35
        const val KEY_J = 36
        const val KEY_K = 37
        const val KEY_L = 38
        const val KEY_SEMICOLON = 39
        const val KEY_APOSTROPHE = 40
        const val KEY_GRAVE = 41
        const val KEY_LEFTSHIFT = 42
        const val KEY_BACKSLASH = 43
        const val KEY_Z = 44
        const val KEY_X = 45
        const val KEY_C = 46
        const val KEY_V = 47
        const val KEY_B = 48
        const val KEY_N = 49
        const val KEY_M = 50
        const val KEY_COMMA = 51
        const val KEY_DOT = 52
        const val KEY_SLASH = 53
        const val KEY_RIGHTSHIFT = 54
        const val KEY_KPASTERISK = 55
        const val KEY_LEFTALT = 56
        const val KEY_SPACE = 57
        const val KEY_CAPSLOCK = 58
        const val KEY_F1 = 59
        const val KEY_F2 = 60
        const val KEY_F3 = 61
        const val KEY_F4 = 62
        const val KEY_F5 = 63
        const val KEY_F6 = 64
        const val KEY_F7 = 65
        const val KEY_F8 = 66
        const val KEY_F9 = 67
        const val KEY_F10 = 68
        const val KEY_NUMLOCK = 69
        const val KEY_SCROLLLOCK = 70
        const val KEY_KP7 = 71
        const val KEY_KP8 = 72
        const val KEY_KP9 = 73
        const val KEY_KPMINUS = 74
        const val KEY_KP4 = 75
        const val KEY_KP5 = 76
        const val KEY_KP6 = 77
        const val KEY_KPPLUS = 78
        const val KEY_KP1 = 79
        const val KEY_KP2 = 80
        const val KEY_KP3 = 81
        const val KEY_KP0 = 82
        const val KEY_KPDOT = 83

        const val KEY_ZENKAKUHANKAKU = 85
        const val KEY_102ND = 86
        const val KEY_F11 = 87
        const val KEY_F12 = 88
        const val KEY_RO = 89
        const val KEY_KATAKANA = 90
        const val KEY_HIRAGANA = 91
        const val KEY_HENKAN = 92
        const val KEY_KATAKANAHIRAGANA = 93
        const val KEY_MUHENKAN = 94
        const val KEY_KPJPCOMMA = 95
        const val KEY_KPENTER = 96
        const val KEY_RIGHTCTRL = 97
        const val KEY_KPSLASH = 98
        const val KEY_SYSRQ = 99
        const val KEY_RIGHTALT = 100
        const val KEY_LINEFEED = 101
        const val KEY_HOME = 102
        const val KEY_UP = 103
        const val KEY_PAGEUP = 104
        const val KEY_LEFT = 105
        const val KEY_RIGHT = 106
        const val KEY_END = 107
        const val KEY_DOWN = 108
        const val KEY_PAGEDOWN = 109
        const val KEY_INSERT = 110
        const val KEY_DELETE = 111
        const val KEY_MACRO = 112
        const val KEY_MUTE = 113
        const val KEY_VOLUMEDOWN = 114
        const val KEY_VOLUMEUP = 115
        const val KEY_POWER = 116    /* SC System Power Down */
        const val KEY_KPEQUAL = 117
        const val KEY_KPPLUSMINUS = 118
        const val KEY_PAUSE = 119
        const val KEY_SCALE = 120    /* AL Compiz Scale (Expose) */

        const val KEY_KPCOMMA = 121
        const val KEY_HANGEUL = 122
        const val KEY_HANGUEL = KEY_HANGEUL
        const val KEY_HANJA = 123
        const val KEY_YEN = 124
        const val KEY_LEFTMETA = 125
        const val KEY_RIGHTMETA = 126
        const val KEY_COMPOSE = 127

        const val KEY_STOP = 128    /* AC Stop */
        const val KEY_AGAIN = 129
        const val KEY_PROPS = 130    /* AC Properties */
        const val KEY_UNDO = 131    /* AC Undo */
        const val KEY_FRONT = 132
        const val KEY_COPY = 133    /* AC Copy */
        const val KEY_OPEN = 134    /* AC Open */
        const val KEY_PASTE = 135    /* AC Paste */
        const val KEY_FIND = 136    /* AC Search */
        const val KEY_CUT = 137    /* AC Cut */
        const val KEY_HELP = 138    /* AL Integrated Help Center */
        const val KEY_MENU = 139    /* Menu (show menu) */
        const val KEY_CALC = 140    /* AL Calculator */
        const val KEY_SETUP = 141
        const val KEY_SLEEP = 142    /* SC System Sleep */
        const val KEY_WAKEUP = 143    /* System Wake Up */
        const val KEY_FILE = 144    /* AL Local Machine Browser */
        const val KEY_SENDFILE = 145
        const val KEY_DELETEFILE = 146
        const val KEY_XFER = 147
        const val KEY_PROG1 = 148
        const val KEY_PROG2 = 149
        const val KEY_WWW = 150    /* AL Internet Browser */
        const val KEY_MSDOS = 151
        const val KEY_COFFEE = 152    /* AL Terminal Lock/Screensaver */
        const val KEY_SCREENLOCK = KEY_COFFEE
        const val KEY_ROTATE_DISPLAY = 153    /* Display orientation for e.g. tablets */
        const val KEY_DIRECTION = KEY_ROTATE_DISPLAY
        const val KEY_CYCLEWINDOWS = 154
        const val KEY_MAIL = 155
        const val KEY_BOOKMARKS = 156    /* AC Bookmarks */
        const val KEY_COMPUTER = 157
        const val KEY_BACK = 158    /* AC Back */
        const val KEY_FORWARD = 159    /* AC Forward */
        const val KEY_CLOSECD = 160
        const val KEY_EJECTCD = 161
        const val KEY_EJECTCLOSECD = 162
        const val KEY_NEXTSONG = 163
        const val KEY_PLAYPAUSE = 164
        const val KEY_PREVIOUSSONG = 165
        const val KEY_STOPCD = 166
        const val KEY_RECORD = 167
        const val KEY_REWIND = 168
        const val KEY_PHONE = 169    /* Media Select Telephone */
        const val KEY_ISO = 170
        const val KEY_CONFIG = 171    /* AL Consumer Control Configuration */
        const val KEY_HOMEPAGE = 172    /* AC Home */
        const val KEY_REFRESH = 173    /* AC Refresh */
        const val KEY_EXIT = 174    /* AC Exit */
        const val KEY_MOVE = 175
        const val KEY_EDIT = 176
        const val KEY_SCROLLUP = 177
        const val KEY_SCROLLDOWN = 178
        const val KEY_KPLEFTPAREN = 179
        const val KEY_KPRIGHTPAREN = 180
        const val KEY_NEW = 181    /* AC New */
        const val KEY_REDO = 182    /* AC Redo/Repeat */

        const val KEY_F13 = 183
        const val KEY_F14 = 184
        const val KEY_F15 = 185
        const val KEY_F16 = 186
        const val KEY_F17 = 187
        const val KEY_F18 = 188
        const val KEY_F19 = 189
        const val KEY_F20 = 190
        const val KEY_F21 = 191
        const val KEY_F22 = 192
        const val KEY_F23 = 193
        const val KEY_F24 = 194

        const val KEY_PLAYCD = 200
        const val KEY_PAUSECD = 201
        const val KEY_PROG3 = 202
        const val KEY_PROG4 = 203
        const val KEY_DASHBOARD = 204    /* AL Dashboard */
        const val KEY_SUSPEND = 205
        const val KEY_CLOSE = 206    /* AC Close */
        const val KEY_PLAY = 207
        const val KEY_FASTFORWARD = 208
        const val KEY_BASSBOOST = 209
        const val KEY_PRINT = 210    /* AC Print */
        const val KEY_HP = 211
        const val KEY_CAMERA = 212
        const val KEY_SOUND = 213
        const val KEY_QUESTION = 214
        const val KEY_EMAIL = 215
        const val KEY_CHAT = 216
        const val KEY_SEARCH = 217
        const val KEY_CONNECT = 218
        const val KEY_FINANCE = 219    /* AL Checkbook/Finance */
        const val KEY_SPORT = 220
        const val KEY_SHOP = 221
        const val KEY_ALTERASE = 222
        const val KEY_CANCEL = 223    /* AC Cancel */
        const val KEY_BRIGHTNESSDOWN = 224
        const val KEY_BRIGHTNESSUP = 225
        const val KEY_MEDIA = 226

        const val KEY_SWITCHVIDEOMODE = 227    /* Cycle between available video
                           outputs (Monitor/LCD/TV-out/etc) */
        const val KEY_KBDILLUMTOGGLE = 228
        const val KEY_KBDILLUMDOWN = 229
        const val KEY_KBDILLUMUP = 230

        const val KEY_SEND = 231    /* AC Send */
        const val KEY_REPLY = 232    /* AC Reply */
        const val KEY_FORWARDMAIL = 233    /* AC Forward Msg */
        const val KEY_SAVE = 234    /* AC Save */
        const val KEY_DOCUMENTS = 235

        const val KEY_BATTERY = 236

        const val KEY_BLUETOOTH = 237
        const val KEY_WLAN = 238
        const val KEY_UWB = 239

        const val KEY_UNKNOWN = 240

        const val KEY_VIDEO_NEXT = 241    /* drive next video source */
        const val KEY_VIDEO_PREV = 242    /* drive previous video source */
        const val KEY_BRIGHTNESS_CYCLE = 243    /* brightness up, after max is min */
        const val KEY_BRIGHTNESS_AUTO = 244    /* Set Auto Brightness: manual
                          brightness control is off,
                          rely on ambient */
        const val KEY_BRIGHTNESS_ZERO = KEY_BRIGHTNESS_AUTO
        const val KEY_DISPLAY_OFF = 245    /* display device to off state */

        const val KEY_WWAN = 246    /* Wireless WAN (LTE, UMTS, GSM, etc.) */
        const val KEY_WIMAX = KEY_WWAN
        const val KEY_RFKILL = 247    /* Key that controls all radios */

        const val KEY_MICMUTE = 248    /* Mute / unmute the microphone */

        /* Code 255 is reserved for special needs of AT keyboard driver */

        const val BTN_MISC = 0x100
        const val BTN_0 = 0x100
        const val BTN_1 = 0x101
        const val BTN_2 = 0x102
        const val BTN_3 = 0x103
        const val BTN_4 = 0x104
        const val BTN_5 = 0x105
        const val BTN_6 = 0x106
        const val BTN_7 = 0x107
        const val BTN_8 = 0x108
        const val BTN_9 = 0x109

        const val BTN_MOUSE = 0x110
        const val BTN_LEFT = 0x110
        const val BTN_RIGHT = 0x111
        const val BTN_MIDDLE = 0x112
        const val BTN_SIDE = 0x113
        const val BTN_EXTRA = 0x114
        const val BTN_FORWARD = 0x115
        const val BTN_BACK = 0x116
        const val BTN_TASK = 0x117

        const val BTN_JOYSTICK = 0x120
        const val BTN_TRIGGER = 0x120
        const val BTN_THUMB = 0x121
        const val BTN_THUMB2 = 0x122
        const val BTN_TOP = 0x123
        const val BTN_TOP2 = 0x124
        const val BTN_PINKIE = 0x125
        const val BTN_BASE = 0x126
        const val BTN_BASE2 = 0x127
        const val BTN_BASE3 = 0x128
        const val BTN_BASE4 = 0x129
        const val BTN_BASE5 = 0x12a
        const val BTN_BASE6 = 0x12b
        const val BTN_DEAD = 0x12f

        const val BTN_GAMEPAD = 0x130
        const val BTN_SOUTH = 0x130
        const val BTN_A = BTN_SOUTH
        const val BTN_EAST = 0x131
        const val BTN_B = BTN_EAST
        const val BTN_C = 0x132
        const val BTN_NORTH = 0x133
        const val BTN_X = BTN_NORTH
        const val BTN_WEST = 0x134
        const val BTN_Y = BTN_WEST
        const val BTN_Z = 0x135
        const val BTN_TL = 0x136
        const val BTN_TR = 0x137
        const val BTN_TL2 = 0x138
        const val BTN_TR2 = 0x139
        const val BTN_SELECT = 0x13a
        const val BTN_START = 0x13b
        const val BTN_MODE = 0x13c
        const val BTN_THUMBL = 0x13d
        const val BTN_THUMBR = 0x13e

        const val BTN_DIGI = 0x140
        const val BTN_TOOL_PEN = 0x140
        const val BTN_TOOL_RUBBER = 0x141
        const val BTN_TOOL_BRUSH = 0x142
        const val BTN_TOOL_PENCIL = 0x143
        const val BTN_TOOL_AIRBRUSH = 0x144
        const val BTN_TOOL_FINGER = 0x145
        const val BTN_TOOL_MOUSE = 0x146
        const val BTN_TOOL_LENS = 0x147
        const val BTN_TOOL_QUINTTAP = 0x148    /* Five fingers on trackpad */
        const val BTN_TOUCH = 0x14a
        const val BTN_STYLUS = 0x14b
        const val BTN_STYLUS2 = 0x14c
        const val BTN_TOOL_DOUBLETAP = 0x14d
        const val BTN_TOOL_TRIPLETAP = 0x14e
        const val BTN_TOOL_QUADTAP = 0x14f    /* Four fingers on trackpad */

        const val BTN_WHEEL = 0x150
        const val BTN_GEAR_DOWN = 0x150
        const val BTN_GEAR_UP = 0x151

        const val KEY_OK = 0x160
        const val KEY_SELECT = 0x161
        const val KEY_GOTO = 0x162
        const val KEY_CLEAR = 0x163
        const val KEY_POWER2 = 0x164
        const val KEY_OPTION = 0x165
        const val KEY_INFO = 0x166    /* AL OEM Features/Tips/Tutorial */
        const val KEY_TIME = 0x167
        const val KEY_VENDOR = 0x168
        const val KEY_ARCHIVE = 0x169
        const val KEY_PROGRAM = 0x16a    /* Media Select Program Guide */
        const val KEY_CHANNEL = 0x16b
        const val KEY_FAVORITES = 0x16c
        const val KEY_EPG = 0x16d
        const val KEY_PVR = 0x16e    /* Media Select Home */
        const val KEY_MHP = 0x16f
        const val KEY_LANGUAGE = 0x170
        const val KEY_TITLE = 0x171
        const val KEY_SUBTITLE = 0x172
        const val KEY_ANGLE = 0x173
        const val KEY_ZOOM = 0x174
        const val KEY_MODE = 0x175
        const val KEY_KEYBOARD = 0x176
        const val KEY_SCREEN = 0x177
        const val KEY_PC = 0x178    /* Media Select Computer */
        const val KEY_TV = 0x179    /* Media Select TV */
        const val KEY_TV2 = 0x17a    /* Media Select Cable */
        const val KEY_VCR = 0x17b    /* Media Select VCR */
        const val KEY_VCR2 = 0x17c    /* VCR Plus */
        const val KEY_SAT = 0x17d    /* Media Select Satellite */
        const val KEY_SAT2 = 0x17e
        const val KEY_CD = 0x17f    /* Media Select CD */
        const val KEY_TAPE = 0x180    /* Media Select Tape */
        const val KEY_RADIO = 0x181
        const val KEY_TUNER = 0x182    /* Media Select Tuner */
        const val KEY_PLAYER = 0x183
        const val KEY_TEXT = 0x184
        const val KEY_DVD = 0x185    /* Media Select DVD */
        const val KEY_AUX = 0x186
        const val KEY_MP3 = 0x187
        const val KEY_AUDIO = 0x188    /* AL Audio Browser */
        const val KEY_VIDEO = 0x189    /* AL Movie Browser */
        const val KEY_DIRECTORY = 0x18a
        const val KEY_LIST = 0x18b
        const val KEY_MEMO = 0x18c    /* Media Select Messages */
        const val KEY_CALENDAR = 0x18d
        const val KEY_RED = 0x18e
        const val KEY_GREEN = 0x18f
        const val KEY_YELLOW = 0x190
        const val KEY_BLUE = 0x191
        const val KEY_CHANNELUP = 0x192    /* Channel Increment */
        const val KEY_CHANNELDOWN = 0x193    /* Channel Decrement */
        const val KEY_FIRST = 0x194
        const val KEY_LAST = 0x195    /* Recall Last */
        const val KEY_AB = 0x196
        const val KEY_NEXT = 0x197
        const val KEY_RESTART = 0x198
        const val KEY_SLOW = 0x199
        const val KEY_SHUFFLE = 0x19a
        const val KEY_BREAK = 0x19b
        const val KEY_PREVIOUS = 0x19c
        const val KEY_DIGITS = 0x19d
        const val KEY_TEEN = 0x19e
        const val KEY_TWEN = 0x19f
        const val KEY_VIDEOPHONE = 0x1a0    /* Media Select Video Phone */
        const val KEY_GAMES = 0x1a1    /* Media Select Games */
        const val KEY_ZOOMIN = 0x1a2    /* AC Zoom In */
        const val KEY_ZOOMOUT = 0x1a3    /* AC Zoom Out */
        const val KEY_ZOOMRESET = 0x1a4    /* AC Zoom */
        const val KEY_WORDPROCESSOR = 0x1a5    /* AL Word Processor */
        const val KEY_EDITOR = 0x1a6    /* AL Text Editor */
        const val KEY_SPREADSHEET = 0x1a7    /* AL Spreadsheet */
        const val KEY_GRAPHICSEDITOR = 0x1a8    /* AL Graphics Editor */
        const val KEY_PRESENTATION = 0x1a9    /* AL Presentation App */
        const val KEY_DATABASE = 0x1aa    /* AL Database App */
        const val KEY_NEWS = 0x1ab    /* AL Newsreader */
        const val KEY_VOICEMAIL = 0x1ac    /* AL Voicemail */
        const val KEY_ADDRESSBOOK = 0x1ad    /* AL Contacts/Address Book */
        const val KEY_MESSENGER = 0x1ae    /* AL Instant Messaging */
        const val KEY_DISPLAYTOGGLE = 0x1af    /* Turn display (LCD) on and off */
        const val KEY_BRIGHTNESS_TOGGLE = KEY_DISPLAYTOGGLE
        const val KEY_SPELLCHECK = 0x1b0   /* AL Spell Check */
        const val KEY_LOGOFF = 0x1b1   /* AL Logoff */

        const val KEY_DOLLAR = 0x1b2
        const val KEY_EURO = 0x1b3

        const val KEY_FRAMEBACK = 0x1b4    /* Consumer - transport controls */
        const val KEY_FRAMEFORWARD = 0x1b5
        const val KEY_CONTEXT_MENU = 0x1b6    /* GenDesc - system context menu */
        const val KEY_MEDIA_REPEAT = 0x1b7    /* Consumer - transport control */
        const val KEY_10CHANNELSUP = 0x1b8    /* 10 channels up (10+) */
        const val KEY_10CHANNELSDOWN = 0x1b9    /* 10 channels down (10-) */
        const val KEY_IMAGES = 0x1ba    /* AL Image Browser */

        const val KEY_DEL_EOL = 0x1c0
        const val KEY_DEL_EOS = 0x1c1
        const val KEY_INS_LINE = 0x1c2
        const val KEY_DEL_LINE = 0x1c3

        const val KEY_FN = 0x1d0
        const val KEY_FN_ESC = 0x1d1
        const val KEY_FN_F1 = 0x1d2
        const val KEY_FN_F2 = 0x1d3
        const val KEY_FN_F3 = 0x1d4
        const val KEY_FN_F4 = 0x1d5
        const val KEY_FN_F5 = 0x1d6
        const val KEY_FN_F6 = 0x1d7
        const val KEY_FN_F7 = 0x1d8
        const val KEY_FN_F8 = 0x1d9
        const val KEY_FN_F9 = 0x1da
        const val KEY_FN_F10 = 0x1db
        const val KEY_FN_F11 = 0x1dc
        const val KEY_FN_F12 = 0x1dd
        const val KEY_FN_1 = 0x1de
        const val KEY_FN_2 = 0x1df
        const val KEY_FN_D = 0x1e0
        const val KEY_FN_E = 0x1e1
        const val KEY_FN_F = 0x1e2
        const val KEY_FN_S = 0x1e3
        const val KEY_FN_B = 0x1e4

        const val KEY_BRL_DOT1 = 0x1f1
        const val KEY_BRL_DOT2 = 0x1f2
        const val KEY_BRL_DOT3 = 0x1f3
        const val KEY_BRL_DOT4 = 0x1f4
        const val KEY_BRL_DOT5 = 0x1f5
        const val KEY_BRL_DOT6 = 0x1f6
        const val KEY_BRL_DOT7 = 0x1f7
        const val KEY_BRL_DOT8 = 0x1f8
        const val KEY_BRL_DOT9 = 0x1f9
        const val KEY_BRL_DOT10 = 0x1fa

        const val KEY_NUMERIC_0 = 0x200    /* used by phones, remote controls, */
        const val KEY_NUMERIC_1 = 0x201    /* and other keypads */
        const val KEY_NUMERIC_2 = 0x202
        const val KEY_NUMERIC_3 = 0x203
        const val KEY_NUMERIC_4 = 0x204
        const val KEY_NUMERIC_5 = 0x205
        const val KEY_NUMERIC_6 = 0x206
        const val KEY_NUMERIC_7 = 0x207
        const val KEY_NUMERIC_8 = 0x208
        const val KEY_NUMERIC_9 = 0x209
        const val KEY_NUMERIC_STAR = 0x20a
        const val KEY_NUMERIC_POUND = 0x20b
        const val KEY_NUMERIC_A = 0x20c    /* Phone key A - HUT Telephony 0xb9 */
        const val KEY_NUMERIC_B = 0x20d
        const val KEY_NUMERIC_C = 0x20e
        const val KEY_NUMERIC_D = 0x20f

        const val KEY_CAMERA_FOCUS = 0x210
        const val KEY_WPS_BUTTON = 0x211    /* WiFi Protected Setup key */

        const val KEY_TOUCHPAD_TOGGLE = 0x212    /* Request switch touchpad on or off */
        const val KEY_TOUCHPAD_ON = 0x213
        const val KEY_TOUCHPAD_OFF = 0x214

        const val KEY_CAMERA_ZOOMIN = 0x215
        const val KEY_CAMERA_ZOOMOUT = 0x216
        const val KEY_CAMERA_UP = 0x217
        const val KEY_CAMERA_DOWN = 0x218
        const val KEY_CAMERA_LEFT = 0x219
        const val KEY_CAMERA_RIGHT = 0x21a

        const val KEY_ATTENDANT_ON = 0x21b
        const val KEY_ATTENDANT_OFF = 0x21c
        const val KEY_ATTENDANT_TOGGLE = 0x21d    /* Attendant call on or off */
        const val KEY_LIGHTS_TOGGLE = 0x21e    /* Reading light on or off */

        const val BTN_DPAD_UP = 0x220
        const val BTN_DPAD_DOWN = 0x221
        const val BTN_DPAD_LEFT = 0x222
        const val BTN_DPAD_RIGHT = 0x223

        const val KEY_ALS_TOGGLE = 0x230    /* Ambient light sensor */

        const val KEY_BUTTONCONFIG = 0x240    /* AL Button Configuration */
        const val KEY_TASKMANAGER = 0x241    /* AL Task/Project Manager */
        const val KEY_JOURNAL = 0x242    /* AL Log/Journal/Timecard */
        const val KEY_CONTROLPANEL = 0x243    /* AL Control Panel */
        const val KEY_APPSELECT = 0x244    /* AL Select Task/Application */
        const val KEY_SCREENSAVER = 0x245    /* AL Screen Saver */
        const val KEY_VOICECOMMAND = 0x246    /* Listening Voice Command */

        const val KEY_BRIGHTNESS_MIN = 0x250    /* Set Brightness to Minimum */
        const val KEY_BRIGHTNESS_MAX = 0x251    /* Set Brightness to Maximum */

        const val KEY_KBDINPUTASSIST_PREV = 0x260
        const val KEY_KBDINPUTASSIST_NEXT = 0x261
        const val KEY_KBDINPUTASSIST_PREVGROUP = 0x262
        const val KEY_KBDINPUTASSIST_NEXTGROUP = 0x263
        const val KEY_KBDINPUTASSIST_ACCEPT = 0x264
        const val KEY_KBDINPUTASSIST_CANCEL = 0x265

        /* Diagonal movement keys */
        const val KEY_RIGHT_UP = 0x266
        const val KEY_RIGHT_DOWN = 0x267
        const val KEY_LEFT_UP = 0x268
        const val KEY_LEFT_DOWN = 0x269

        const val KEY_ROOT_MENU = 0x26a /* Show Device's Root Menu */

        /* Show Top Menu of the Media (e.g. DVD) */
        const val KEY_MEDIA_TOP_MENU = 0x26b
        const val KEY_NUMERIC_11 = 0x26c
        const val KEY_NUMERIC_12 = 0x26d

        /*
         * Toggle Audio Description: refers to an audio service that helps blind and
         * visually impaired consumers understand the action in a program. Note: in
         * some countries this is referred to as "Video Description".
         */
        const val KEY_AUDIO_DESC = 0x26e
        const val KEY_3D_MODE = 0x26f
        const val KEY_NEXT_FAVORITE = 0x270
        const val KEY_STOP_RECORD = 0x271
        const val KEY_PAUSE_RECORD = 0x272
        const val KEY_VOD = 0x273 /* Video on Demand */
        const val KEY_UNMUTE = 0x274
        const val KEY_FASTREVERSE = 0x275
        const val KEY_SLOWREVERSE = 0x276

        /*
         * Control a data application associated with the currently viewed channel,
         * e.g. teletext or data broadcast application (MHEG, MHP, HbbTV, etc.)
         */
        const val KEY_DATA = 0x275

        const val BTN_TRIGGER_HAPPY = 0x2c0
        const val BTN_TRIGGER_HAPPY1 = 0x2c0
        const val BTN_TRIGGER_HAPPY2 = 0x2c1
        const val BTN_TRIGGER_HAPPY3 = 0x2c2
        const val BTN_TRIGGER_HAPPY4 = 0x2c3
        const val BTN_TRIGGER_HAPPY5 = 0x2c4
        const val BTN_TRIGGER_HAPPY6 = 0x2c5
        const val BTN_TRIGGER_HAPPY7 = 0x2c6
        const val BTN_TRIGGER_HAPPY8 = 0x2c7
        const val BTN_TRIGGER_HAPPY9 = 0x2c8
        const val BTN_TRIGGER_HAPPY10 = 0x2c9
        const val BTN_TRIGGER_HAPPY11 = 0x2ca
        const val BTN_TRIGGER_HAPPY12 = 0x2cb
        const val BTN_TRIGGER_HAPPY13 = 0x2cc
        const val BTN_TRIGGER_HAPPY14 = 0x2cd
        const val BTN_TRIGGER_HAPPY15 = 0x2ce
        const val BTN_TRIGGER_HAPPY16 = 0x2cf
        const val BTN_TRIGGER_HAPPY17 = 0x2d0
        const val BTN_TRIGGER_HAPPY18 = 0x2d1
        const val BTN_TRIGGER_HAPPY19 = 0x2d2
        const val BTN_TRIGGER_HAPPY20 = 0x2d3
        const val BTN_TRIGGER_HAPPY21 = 0x2d4
        const val BTN_TRIGGER_HAPPY22 = 0x2d5
        const val BTN_TRIGGER_HAPPY23 = 0x2d6
        const val BTN_TRIGGER_HAPPY24 = 0x2d7
        const val BTN_TRIGGER_HAPPY25 = 0x2d8
        const val BTN_TRIGGER_HAPPY26 = 0x2d9
        const val BTN_TRIGGER_HAPPY27 = 0x2da
        const val BTN_TRIGGER_HAPPY28 = 0x2db
        const val BTN_TRIGGER_HAPPY29 = 0x2dc
        const val BTN_TRIGGER_HAPPY30 = 0x2dd
        const val BTN_TRIGGER_HAPPY31 = 0x2de
        const val BTN_TRIGGER_HAPPY32 = 0x2df
        const val BTN_TRIGGER_HAPPY33 = 0x2e0
        const val BTN_TRIGGER_HAPPY34 = 0x2e1
        const val BTN_TRIGGER_HAPPY35 = 0x2e2
        const val BTN_TRIGGER_HAPPY36 = 0x2e3
        const val BTN_TRIGGER_HAPPY37 = 0x2e4
        const val BTN_TRIGGER_HAPPY38 = 0x2e5
        const val BTN_TRIGGER_HAPPY39 = 0x2e6
        const val BTN_TRIGGER_HAPPY40 = 0x2e7

        /* We avoid low common keys in module aliases so they don't get huge. */
        const val KEY_MIN_INTERESTING = KEY_MUTE
        const val KEY_MAX = 0x2ff
        const val KEY_CNT = (KEY_MAX + 1)

        /*
     * Relative axes
     */

        const val REL_X = 0x00
        const val REL_Y = 0x01
        const val REL_Z = 0x02
        const val REL_RX = 0x03
        const val REL_RY = 0x04
        const val REL_RZ = 0x05
        const val REL_HWHEEL = 0x06
        const val REL_DIAL = 0x07
        const val REL_WHEEL = 0x08
        const val REL_MISC = 0x09
        const val REL_MAX = 0x0f
        const val REL_CNT = (REL_MAX + 1)

        /*
     * Absolute axes
     */

        const val ABS_X = 0x00
        const val ABS_Y = 0x01
        const val ABS_Z = 0x02
        const val ABS_RX = 0x03
        const val ABS_RY = 0x04
        const val ABS_RZ = 0x05
        const val ABS_THROTTLE = 0x06
        const val ABS_RUDDER = 0x07
        const val ABS_WHEEL = 0x08
        const val ABS_GAS = 0x09
        const val ABS_BRAKE = 0x0a
        const val ABS_HAT0X = 0x10
        const val ABS_HAT0Y = 0x11
        const val ABS_HAT1X = 0x12
        const val ABS_HAT1Y = 0x13
        const val ABS_HAT2X = 0x14
        const val ABS_HAT2Y = 0x15
        const val ABS_HAT3X = 0x16
        const val ABS_HAT3Y = 0x17
        const val ABS_PRESSURE = 0x18
        const val ABS_DISTANCE = 0x19
        const val ABS_TILT_X = 0x1a
        const val ABS_TILT_Y = 0x1b
        const val ABS_TOOL_WIDTH = 0x1c

        const val ABS_VOLUME = 0x20

        const val ABS_MISC = 0x28

        const val ABS_MT_SLOT = 0x2f    /* MT slot being modified */
        const val ABS_MT_TOUCH_MAJOR = 0x30    /* Major axis of touching ellipse */
        const val ABS_MT_TOUCH_MINOR = 0x31    /* Minor axis (omit if circular) */
        const val ABS_MT_WIDTH_MAJOR = 0x32    /* Major axis of approaching ellipse */
        const val ABS_MT_WIDTH_MINOR = 0x33    /* Minor axis (omit if circular) */
        const val ABS_MT_ORIENTATION = 0x34    /* Ellipse orientation */
        const val ABS_MT_POSITION_X = 0x35    /* Center X touch position */
        const val ABS_MT_POSITION_Y = 0x36    /* Center Y touch position */
        const val ABS_MT_TOOL_TYPE = 0x37    /* Type of touching device */
        const val ABS_MT_BLOB_ID = 0x38    /* Group a set of packets as a blob */
        const val ABS_MT_TRACKING_ID = 0x39    /* Unique ID of initiated contact */
        const val ABS_MT_PRESSURE = 0x3a    /* Pressure on contact area */
        const val ABS_MT_DISTANCE = 0x3b    /* Contact hover distance */
        const val ABS_MT_TOOL_X = 0x3c    /* Center X tool position */
        const val ABS_MT_TOOL_Y = 0x3d    /* Center Y tool position */


        const val ABS_MAX = 0x3f
        const val ABS_CNT = (ABS_MAX + 1)

        /*
     * Switch events
     */

        const val SW_LID = 0x00  /* set = lid shut */
        const val SW_TABLET_MODE = 0x01  /* set = tablet mode */
        const val SW_HEADPHONE_INSERT = 0x02  /* set = inserted */
        const val SW_RFKILL_ALL = 0x03  /* rfkill master switch, type "any"
                         set = radio enabled */
        const val SW_RADIO = SW_RFKILL_ALL    /* deprecated */
        const val SW_MICROPHONE_INSERT = 0x04  /* set = inserted */
        const val SW_DOCK = 0x05  /* set = plugged into dock */
        const val SW_LINEOUT_INSERT = 0x06  /* set = inserted */
        const val SW_JACK_PHYSICAL_INSERT = 0x07  /* set = mechanical switch set */
        const val SW_VIDEOOUT_INSERT = 0x08  /* set = inserted */
        const val SW_CAMERA_LENS_COVER = 0x09  /* set = lens covered */
        const val SW_KEYPAD_SLIDE = 0x0a  /* set = keypad slide out */
        const val SW_FRONT_PROXIMITY = 0x0b  /* set = front proximity sensor active */
        const val SW_ROTATE_LOCK = 0x0c  /* set = rotate locked/disabled */
        const val SW_LINEIN_INSERT = 0x0d  /* set = inserted */
        const val SW_MUTE_DEVICE = 0x0e  /* set = device disabled */
        const val SW_PEN_INSERTED = 0x0f  /* set = pen inserted */
        const val SW_MAX = 0x0f
        const val SW_CNT = (SW_MAX + 1)

        /*
     * Misc events
     */

        const val MSC_SERIAL = 0x00
        const val MSC_PULSELED = 0x01
        const val MSC_GESTURE = 0x02
        const val MSC_RAW = 0x03
        const val MSC_SCAN = 0x04
        const val MSC_TIMESTAMP = 0x05
        const val MSC_MAX = 0x07
        const val MSC_CNT = (MSC_MAX + 1)

        /*
     * LEDs
     */

        const val LED_NUML = 0x00
        const val LED_CAPSL = 0x01
        const val LED_SCROLLL = 0x02
        const val LED_COMPOSE = 0x03
        const val LED_KANA = 0x04
        const val LED_SLEEP = 0x05
        const val LED_SUSPEND = 0x06
        const val LED_MUTE = 0x07
        const val LED_MISC = 0x08
        const val LED_MAIL = 0x09
        const val LED_CHARGING = 0x0a
        const val LED_MAX = 0x0f
        const val LED_CNT = (LED_MAX + 1)

        /*
     * Autorepeat values
     */

        const val REP_DELAY = 0x00
        const val REP_PERIOD = 0x01
        const val REP_MAX = 0x01
        const val REP_CNT = (REP_MAX + 1)

        /*
     * Sounds
     */

        const val SND_CLICK = 0x00
        const val SND_BELL = 0x01
        const val SND_TONE = 0x02
        const val SND_MAX = 0x07
        const val SND_CNT = (SND_MAX + 1)

//    }
//}