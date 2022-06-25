//
// Created by ductt on 12/23/2018.
//

#include <unistd.h> // usleep
#include <android/log.h>

#include "PesGamepad.hpp"


#define LTAG "PesMapping"
//#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, LTAG, __VA_ARGS__)

PesGamepad::PesGamepad() {
    mt = new UdevTouch("Virtual Touch");
    left_touch = mt->new_touch();
    right_touch = mt->new_touch();
}

PesGamepad::~PesGamepad() {
    if (mt != NULL) delete mt;
}

PesGamepad* PesGamepad::set_device(Udev* gamepad) {
    StoppablePoll::set_device(gamepad);
    set_callback(this);
    return this;
}

void PesGamepad::eventReceived(Udev *, input_event *) {}

void PesGamepad::eventCodeReceived(Udev*, int code, float value) {
    switch (code) {
        case(GPAD_CB_KA):
        case(GPAD_CB_L1): right_touch_handle(PASS_SWITCH, value); break;
        case(GPAD_CB_KB): right_touch_handle(CROSS, value); break;
        case(GPAD_CB_KX): right_touch_handle(SHOT_CLEAR, value); break;
        case(GPAD_CB_KY): right_touch_handle(THROUGH_PRESS, value); break;
        case(GPAD_CB_R1): right_touch_handle(DASH, value); break;
        default: LOGD("Unsupport keymap: %d %f", code, value);
    }
}

void PesGamepad::right_touch_handle(int code, float value) {
    if (value==0) right_touch->release();
    else {
        if (right_touch->activated()) {
            usleep(10 * 1000);
            right_touch->release();
        }

        switch (code) {
            case (PASS_SWITCH): right_touch->active(1475, 935); break;
            case (SHOT_CLEAR): right_touch->active(1775, 635); break;
            case (THROUGH_PRESS): right_touch->active(1535, 695); break;
            case (DASH): right_touch->active(1760, 910); break;
            default: LOGD("Unsupported right touch: %d", code);
        }
    }
}

void PesGamepad::left_touch_handle(int code, float value) {

}
