//
// Created by ductt on 12/18/2018.
//

#ifndef EVENTINJECTOR_UINPUT_HELPER_HPP
#define EVENTINJECTOR_UINPUT_HELPER_HPP

#include <string>

#include "Udev.hpp"

#define DEVTOUCH_MAXID 65536
#define DEVTOUCH_MAXSLOT 10
#define DEVTOUCH_INTERVAL 10  /* in ms */

class UdevTouch;

class UdevTouchPoint {
private:
    UdevTouch* p;
    int fd;
    uint slot;
    int id;
    int x;
    int y;
    void change_slot();
public:
    UdevTouchPoint(UdevTouch* parent, int fd, uint slot);
    bool activated();
    void active(int x, int y);
    void move(int x, int y);
    void rmove(int rx, int ry);
    void swipe(int x, int y, int inms);
    void rswipe(int rx, int ry, int inms);
    void release();
    uint index();
};

class UdevTouch : public Udev {
protected:
    UdevTouchPoint* slots [DEVTOUCH_MAXSLOT];
    bool slotused [DEVTOUCH_MAXSLOT];
    int curslot;
    int curtrackid;
    uint total_touchs;
    uinput_user_dev uinp;
    void create_virtual_mt(uint width, uint height);
public:
    UdevTouch(std::string name);
    UdevTouch(std::string name, uint width, uint height);
    ~UdevTouch();
    using Udev::open;   // inherit from parent
    void open();
    void change_slot(uint slot);
    uint next_trackid();
    UdevTouchPoint* new_touch();
    void unleased_touch(UdevTouchPoint* tp);
    void touch_active(uint slot);
    void touch_release(uint slot);
};

#endif //EVENTINJECTOR_UINPUT_HELPER_HPP
