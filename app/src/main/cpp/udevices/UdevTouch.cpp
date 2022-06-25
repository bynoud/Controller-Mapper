//
// Created by ductt on 12/18/2018.
//

#include <fcntl.h>
#include <errno.h>
#include <unistd.h>

#include <android/log.h>

#include "UdevTouch.hpp"


using namespace std;

#define LTAG "UdevTouch"
//#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, LTAG, __VA_ARGS__)


#define TRY_CMD_MSG(cmd, msg) if (cmd < 0) { \
    string logmsg(#cmd); logmsg.append("msg="); logmsg.append(msg); \
    logmsg.append(" : "); logmsg.append(strerror(errno)); \
    throw UdevException(logmsg);}

#define TRY_CMD(cmd) TRY_CMD_MSG(cmd, "NONE")

#define TRY_SET(v, cmd) if ((v=cmd) <0) { \
    string logmsg(#v); logmsg.append("="); logmsg.append(#cmd); \
    logmsg.append(": "); logmsg.append(strerror(errno)); \
    throw UdevException(logmsg);}

#define ASSERTD(exp, msg) if (!(exp)) { \
    string logmsg(#exp); logmsg.append(" : "); logmsg.append(msg); \
    throw UdevException(logmsg); }

// Touch point
UdevTouchPoint::UdevTouchPoint(UdevTouch* parent, int fd, uint slot) {
    this->p = parent;
    this->fd = fd;
    this->slot = slot;
    id = -1;
    x = 0;
    y = 0;
}

uint UdevTouchPoint::index() { return slot; }

bool UdevTouchPoint::activated() {
    return (id>0);
}

void UdevTouchPoint::change_slot() {
    ASSERTD(id>0, "Slot not active")
    p->change_slot(slot);
}

void UdevTouchPoint::active(int x, int y){
    ASSERTD(id<0, "Slot already activated")
    //throw UdevException((string)"Slot " + to_string(slot) + " alredy activated");
    p->change_slot(slot);
    id = p->next_trackid();
    p->send_event(EV_ABS, ABS_MT_TRACKING_ID, id);
    p->send_event(EV_ABS, ABS_MT_POSITION_X, x);
    p->send_event(EV_ABS, ABS_MT_POSITION_Y, y);
    p->touch_active(slot);
    p->send_event(EV_SYN, SYN_REPORT, 0);
    this->x = x;
    this->y = y;
}

void UdevTouchPoint::move(int x, int y) {
    change_slot();
    p->send_event(EV_ABS, ABS_MT_POSITION_X, x);
    p->send_event(EV_ABS, ABS_MT_POSITION_Y, y);
    p->send_event(EV_SYN, SYN_REPORT, 0);
    this->x = x;
    this->y = y;
}

void UdevTouchPoint::rmove(int rx, int ry) {
    move(rx+x, ry+y);
}

void UdevTouchPoint::swipe(int x, int y, int inms) {
    change_slot();
    int n = (int)((float)inms / DEVTOUCH_INTERVAL);
    float cx = (float)this->x;
    float cy = (float)this->y;
    float dx = ((float)x - cx) / n;
    float dy = ((float)y - cy) / n;
    for (int i=0; i<n; i++) {
        usleep(DEVTOUCH_INTERVAL*1000);
        cx += dx; cy += dy;
        this->move((int)cx, (int)cy);
    }

}

void UdevTouchPoint::rswipe(int rx, int ry, int inms) {
    swipe(x+rx, y+ry, inms);
}

void UdevTouchPoint::release() {
    if (!activated()) return;
    change_slot();
    p->send_event(EV_ABS, ABS_MT_TRACKING_ID, -1);
    p->touch_release(slot);
    p->send_event(EV_SYN, SYN_REPORT, 0);
    id = -1;
}

// Touch device
UdevTouch::UdevTouch(std::string name) : UdevTouch(name, 1080, 1920) {};
UdevTouch::UdevTouch(std::string name, uint width, uint height) : Udev(name, false) {
    open();
    create_virtual_mt(width, height);
    close();
};

void UdevTouch::open() { open(O_WRONLY | O_NONBLOCK); }

void UdevTouch::create_virtual_mt(uint width, uint height) {
    LOGD("prrepair to create virmt: %d", fd);
    has_mtouch = true;

    for (uint i=0; i<DEVTOUCH_MAXSLOT; i++) {
        slots[i] = new UdevTouchPoint(this, fd, i);
        slotused[i] = 0;
    }
    curslot = -1;
    curtrackid = 0;
    total_touchs = 0;

    // configure touch device event properties
    uinp = uinput_user_dev();
    strncpy(uinp.name, name.c_str(), UINPUT_MAX_NAME_SIZE);
    uinp.id.version = 4;
    uinp.id.bustype = BUS_USB;
    uinp.absmin[ABS_MT_SLOT] = 0;
    uinp.absmax[ABS_MT_SLOT] = DEVTOUCH_MAXSLOT - 1; // track up to 9 fingers
    //uinp.absmin[ABS_MT_TOUCH_MAJOR] = 0;
    //uinp.absmax[ABS_MT_TOUCH_MAJOR] = 15;
    uinp.absmin[ABS_MT_POSITION_X] = 0; // screen dimension
    uinp.absmax[ABS_MT_POSITION_X] = width-1; // screen dimension
    uinp.absmin[ABS_MT_POSITION_Y] = 0; // screen dimension
    uinp.absmax[ABS_MT_POSITION_Y] = height-1; // screen dimension
    uinp.absmin[ABS_MT_TRACKING_ID] = 0;
    uinp.absmax[ABS_MT_TRACKING_ID] = DEVTOUCH_MAXID - 1;
    //uinp.absmin[ABS_MT_PRESSURE] = 0;
    //uinp.absmax[ABS_MT_PRESSURE] = 255;

    // Setup the uinput device
    TRY_CMD(ioctl(fd, UI_SET_EVBIT, EV_SYN))

    //ioctl(fd, UI_SET_EVBIT, EV_REL);

    // Touch
    TRY_CMD(ioctl(fd, UI_SET_EVBIT, EV_ABS))
    TRY_CMD(ioctl(fd, UI_SET_ABSBIT, ABS_MT_SLOT))
    //TRY_CMD(ioctl(fd, UI_SET_ABSBIT, ABS_MT_TOUCH_MAJOR))
    TRY_CMD(ioctl(fd, UI_SET_ABSBIT, ABS_MT_POSITION_X))
    TRY_CMD(ioctl(fd, UI_SET_ABSBIT, ABS_MT_POSITION_Y))
    TRY_CMD(ioctl(fd, UI_SET_ABSBIT, ABS_MT_TRACKING_ID))
    //TRY_CMD(ioctl(fd, UI_SET_ABSBIT, ABS_MT_PRESSURE))
    TRY_CMD(ioctl(fd, UI_SET_PROPBIT, INPUT_PROP_DIRECT))

    // xiaomi need this?
    TRY_CMD(ioctl(fd, UI_SET_EVBIT, EV_KEY))
    TRY_CMD(ioctl(fd, UI_SET_KEYBIT, BTN_TOUCH))


    /* Create input device into input sub-system */
    TRY_CMD(write(fd, &uinp, sizeof(uinp)))
    TRY_CMD(ioctl(fd, UI_DEV_CREATE))

}

UdevTouch::~UdevTouch() {
    LOGD("it destroying");
    ioctl(fd, UI_DEV_DESTROY);
    for (int i=0; i<DEVTOUCH_MAXSLOT; i++) {
        slots[i]->release();
        delete slots[i];
    }
}

UdevTouchPoint* UdevTouch::new_touch(){
    for (uint i=0; i < DEVTOUCH_MAXSLOT ; i++) {
        if (!slotused[i]) {
            LOGD("new touch: %d", i);
            slotused[i] = 1;
            return slots[i];
        }
    }
    ASSERTD(0, "Out of slot");
}

void UdevTouch::unleased_touch(UdevTouchPoint *tp) {
    if (tp->activated()) tp->release();
    slotused[tp->index()] = 0;
}

void UdevTouch::change_slot(uint slot) {
    if (curslot != slot) {
        send_event(EV_ABS, ABS_MT_SLOT, slot);
        curslot = slot;
    }
}

uint UdevTouch::next_trackid() {
    curtrackid++;
    if (curtrackid >= DEVTOUCH_MAXID) { curtrackid = 1; }
    return (uint)curtrackid;
}

void UdevTouch::touch_active(uint slot) {
    if (total_touchs == 0) {
        send_event(EV_KEY, BTN_TOUCH, 1); // set this at first touch active
    }
    total_touchs++;
}

void UdevTouch::touch_release(uint slot) {
    total_touchs--;
    if (total_touchs == 0) {
        send_event(EV_KEY, BTN_TOUCH, 0); // clear when all touch released
    }
}
