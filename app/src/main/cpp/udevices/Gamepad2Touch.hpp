//
// Created by ductt on 12/20/2018.
//

#ifndef EVENTINJECTOR_UDEVMAPPER_HPP
#define EVENTINJECTOR_UDEVMAPPER_HPP

#include <vector>

#include "Udev.hpp"
#include "UdevTouch.hpp"
#include "IUdevMapper.hpp"

class UdevMapper : public IUdevMapper {
private:
    std::vector<Udev*> in_srcs;
    UdevTouch* out_src;
public:
    int on_event(Udev* src);
    void start();
    void stop();
    void add_input_source(Udev* insrc);
    void set_output_source(Udev* outsrc);
};

#endif //EVENTINJECTOR_UDEVMAPPER_HPP
