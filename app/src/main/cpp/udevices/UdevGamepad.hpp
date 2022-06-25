//
// Created by ductt on 12/20/2018.
//

#ifndef EVENTINJECTOR_UDEVGAMEPAD_HPP
#define EVENTINJECTOR_UDEVGAMEPAD_HPP

#include <linux/input.h>

#include <string>

#include "Udev.hpp"

enum Gpad_key {
    GPAD_KA, GPAD_KB, GPAD_KX, GPAD_KY,
    GPAD_KLeft, GPAD_KRight, GPAD_KUp, GPAD_KDown, // Dpad left/right/up/down
    GPAD_KStart, GPAD_KSelect,
    GPAD_KL1, GPAD_KL2, GPAD_KL3, // L1 2 3
    GPAD_KR1, GPAD_KR2, GPAD_KR3,
    GPAD_KMAX
};

enum Gpad_joy { GPAD_JR, GPAD_JL, GPAD_JMAX};
enum Gpad_dim { GPAD_DX, GPAD_DY, GPAD_DMAX};

enum Gpad_callback {
    GPAD_CB_KA, GPAD_CB_KB, GPAD_CB_KX, GPAD_CB_KY,
    GPAD_CB_KLeft, GPAD_CB_KRight, GPAD_CB_KUp, GPAD_CB_KDown,
    GPAD_CB_Start, GPAD_CB_Select,
    GPAD_CB_L1, GPAD_CB_L2, GPAD_CB_L3,
    GPAD_CB_R1, GPAD_CB_R2, GPAD_CB_R3,
    GPAD_CB_JR, GPAD_CB_JL,
    GPAD_CB_MAX
};

typedef void (*onEventCodeReceived)(int, float);

class UdevGamepad : public Udev {
private:
    bool keys [GPAD_KMAX]; // bit set is pressed, cleared is released
    float motions [GPAD_JMAX] [GPAD_DMAX];
    int flat;   // unit in raw report, not percentage
    //bool polling;
    void update_state(input_event* ev);
public:
    UdevGamepad(std::string path, std::string name);
    ~UdevGamepad();
    void set_flat(int f);
    void poll(input_event* ev, int timeoutms, short selectevent);
    bool getKeyState(int key);
    float getJoyX(int joy);
    float getJoyY(int joy);
    //void continue_poll(IUdevMapper* mapper, int timeoutms);
    //void stop_poll();
};

#endif //EVENTINJECTOR_UDEVGAMEPAD_HPP
