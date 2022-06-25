//
// Created by ductt on 12/20/2018.
//

#ifndef EVENTINJECTOR_IUDEVMAPPER_HPP
#define EVENTINJECTOR_IUDEVMAPPER_HPP

class Udev;

class IUdevMapper {
public:
    IUdevMapper() {}

    virtual ~IUdevMapper() {}

    virtual int on_event(Udev* src) = 0; // pure virtual
    virtual void start() =0;
    virtual void stop() =0;
    virtual void add_input_source(Udev* insrc) =0;
    virtual void set_output_source(Udev* outsrc) =0;
};

#endif //EVENTINJECTOR_IUDEVMAPPER_HPP
