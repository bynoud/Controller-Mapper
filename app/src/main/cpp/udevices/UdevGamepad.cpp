//
// Created by ductt on 12/20/2018.
//

#include <string>
#include <android/log.h>

#include "UdevGamepad.hpp"
#include "IUdevMapper.hpp"

using namespace std;

#define LTAG "UdevGamepad"
//#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, LTAG, __VA_ARGS__)

UdevGamepad::UdevGamepad(string path, string name) : Udev(path, name) {
    for (int i = 0; i < GPAD_KMAX; i++) keys[i] = 0;
    motions[GPAD_JL][GPAD_DX] = 0;
    motions[GPAD_JL][GPAD_DY] = 0;
    motions[GPAD_JR][GPAD_DX] = 0;
    motions[GPAD_JR][GPAD_DY] = 0;
    flat = 1;
}

UdevGamepad::~UdevGamepad() {}

void UdevGamepad::set_flat(int f) { flat = f; }

bool UdevGamepad::getKeyState(int key) { return keys[key]; }
float UdevGamepad::getJoyX(int joy) { return motions[joy][GPAD_DX]; }
float UdevGamepad::getJoyY(int joy) { return motions[joy][GPAD_DY]; }

void UdevGamepad::poll(input_event* ev, int timeoutms, short selectevent) {
    Udev::poll(ev, timeoutms, selectevent);
    update_state(ev);
    LOGD(" keys: A %d B %d X %d Y %d L1 %d L2 %d L3 %d R1 %d R2 %d R3 %d select %d start %d",
         keys[GPAD_KA], keys[GPAD_KB], keys[GPAD_KX], keys[GPAD_KY],
         keys[GPAD_KL1], keys[GPAD_KL2], keys[GPAD_KL3],
         keys[GPAD_KR1], keys[GPAD_KR2], keys[GPAD_KR3],
         keys[GPAD_KSelect], keys[GPAD_KStart]);
    LOGD(" moti: left %d right %d up %d down %d leftjoy (%f , %f) rightjoy (%f , %f)",
         keys[GPAD_KLeft], keys[GPAD_KRight], keys[GPAD_KUp], keys[GPAD_KDown],
         motions[GPAD_JL][GPAD_DX], motions[GPAD_JL][GPAD_DY],
         motions[GPAD_JR][GPAD_DX], motions[GPAD_JR][GPAD_DY]);
}

void UdevGamepad::update_state(input_event* ev) {
    int cbcode = GPAD_CB_MAX;
    float cbflt = 1.0f;
    bool v = (bool) ev->value;

    switch (ev->type) {
        case EV_ABS:
            switch (ev->code) {
                case ABS_HAT0X:
                    if (ev->value < 0) {
                        keys[GPAD_KLeft] = 1;
                        cbcode = GPAD_CB_KLeft;
                    }
                    else if (ev->value > 0) {
                        keys[GPAD_KRight] = 1;
                        cbcode = GPAD_CB_KRight;
                    } else {
                        if (keys[GPAD_KLeft]) cbcode = GPAD_CB_KLeft;
                        else if (keys[GPAD_KRight]) cbcode = GPAD_CB_KRight;
                        cbflt = 0;
                        keys[GPAD_KLeft] = 0;
                        keys[GPAD_KRight] = 0;
                    }
                    break;
                case ABS_HAT0Y:
                    if (ev->value < 0) {
                        keys[GPAD_KDown] = 1;
                        cbcode = GPAD_CB_KDown;
                    }
                    else if (ev->value > 0) {
                        keys[GPAD_KUp] = 1;
                        cbcode = GPAD_CB_KUp;
                    } else {
                        if (keys[GPAD_KDown]) cbcode = GPAD_CB_KDown;
                        else if (keys[GPAD_KUp]) cbcode = GPAD_CB_KUp;
                        cbflt = 0;
                        keys[GPAD_KUp] = 0;
                        keys[GPAD_KDown] = 0;
                    }
                    break;

                default:
                    int joy = (ev->code == ABS_X || ev->code == ABS_Y) ? GPAD_JL : GPAD_JR;
                    int dim = (ev->code == ABS_X || ev->code == ABS_Z) ? GPAD_DX : GPAD_DY;
                    // value is from 0->255, 128 is idle, relative value is -128 -> 127
                    // to normalize [-1,1] it will not reach 1 as maximum
                    if (ev->value >= (128 - flat) || (ev->value <= (128 + flat))) {
                        motions[joy][dim] = 0;
                    } else {
                        motions[joy][dim] = ((float) ev->value - 128.0f) / 128.0f;
                    }
                    cbcode = (joy==GPAD_JL) ? GPAD_CB_JL : GPAD_CB_JR;
                    cbflt = motions[joy][dim];
            }
            break;

        case EV_KEY:
            cbflt = v;
            switch (ev->code) {
                case BTN_A:
                    keys[GPAD_KA] = v;
                    cbcode = GPAD_CB_KA;
                    break;
                case BTN_B:
                    keys[GPAD_KB] = v;
                    cbcode = GPAD_CB_KB;
                    break;
                case BTN_X:
                    keys[GPAD_KX] = v;
                    cbcode = GPAD_CB_KX;
                    break;
                case BTN_Y:
                    keys[GPAD_KY] = v;
                    cbcode = GPAD_CB_KY;
                    break;
                case BTN_TR:
                    keys[GPAD_KR1] = v;
                    cbcode = GPAD_CB_R1;
                    break;
                case BTN_TR2:
                    keys[GPAD_KR2] = v;
                    cbcode = GPAD_CB_R2;
                    break;
                case BTN_THUMBR:
                    keys[GPAD_KR3] = v;
                    cbcode = GPAD_CB_R3;
                    break;
                case BTN_TL:
                    keys[GPAD_KL1] = v;
                    cbcode = GPAD_CB_L1;
                    break;
                case BTN_TL2:
                    keys[GPAD_KL2] = v;
                    cbcode = GPAD_CB_L2;
                    break;
                case BTN_THUMBL:
                    keys[GPAD_KL3] = v;
                    cbcode = GPAD_CB_L3;
                    break;
                case BTN_SELECT:
                    keys[GPAD_KSelect] = v;
                    cbcode = GPAD_CB_Select;
                    break;
                case BTN_START:
                    keys[GPAD_KStart] = v;
                    cbcode = GPAD_CB_Start;
                    break;
                default:
                    LOGD("Unknown KEY event");
            }
            break;

        default:
            LOGD("Unknown event");
    }
    if (eventCb != NULL) eventCb->eventCodeReceived(this, cbcode, cbflt);
}
