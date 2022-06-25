//
// Created by ductt on 12/23/2018.
//

#ifndef KEYMAPPER_PESGAMEPAD_HPP
#define KEYMAPPER_PESGAMEPAD_HPP

#include "UdevGamepad.hpp"
#include "UdevTouch.hpp"
#include "Stoppable.cpp"

enum PesControls {
    MOVE,
    PASS_SWITCH, CROSS, THROUGH_PRESS,
    CALL, SHOT_CLEAR,
    CHIPSHOT, CONTROLEDSHOT,
    DASH, QUICKSTOP
};

class PesGamepad : public IDevEventListener, public StoppablePoll {
private:
    UdevTouch* mt;
    UdevTouchPoint* left_touch;
    UdevTouchPoint* right_touch;
    void left_touch_handle(int code, float value);
    void right_touch_handle(int code, float value);
public:
    PesGamepad();
    ~PesGamepad();
    PesGamepad* set_device(Udev* gamepad);
    void eventReceived(Udev*, input_event*);
    void eventCodeReceived(Udev*, int code, float value);
};

#endif //KEYMAPPER_PESGAMEPAD_HPP
