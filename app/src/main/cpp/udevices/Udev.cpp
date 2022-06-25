//
// Created by ductt on 12/19/2018.
//
#include <fcntl.h>
#include <poll.h>
#include <errno.h>
#include <unistd.h>
#include <dirent.h> // DIR dirent

#include <android/log.h>

const auto& libpoll = poll; // confilict with class function name
const auto& libopen = static_cast<int(*)(const char*, int, ...)>(open); //open;
const auto& libclose = close;


#include "Udev.hpp"


using namespace std;

void emptyFn() {}

#define LTAG "Udev"
//#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, LTAG, __VA_ARGS__)
//#define LOGD(...) emptyFn()


#define TRY_CMD_MSG(cmd, msg) if (cmd < 0) { \
    string logmsg(#cmd); logmsg.append("msg="); logmsg.append(msg); \
    logmsg.append(" : "); logmsg.append(strerror(errno)); \
    throw UdevException(logmsg);}

#define TRY_CMD(cmd) TRY_CMD_MSG(cmd, "NONE")

#define TRY_SET(v, cmd) if ((v=cmd) <0) { \
    string logmsg(#v); logmsg.append("="); logmsg.append(#cmd); \
    logmsg.append(": "); logmsg.append(strerror(errno)); \
    throw UdevException(logmsg);}



// simple event callback
void UdevEventLogging::eventReceived(Udev*, input_event* ev) {
    LOGD("cakkback is called: %d %d %d", ev->code, ev->type, ev->value);
    cout << "EV " << ev->code << " " << ev->type << " " << ev->value << endl;
}
void UdevEventLogging::eventCodeReceived(Udev*, int code, float value) {
    LOGD("cakkback code is called: %d %f", code, value);
    cout << "Code " << code << " " << value << endl;
}



Udev::Udev(string path): Udev(path, true) {};

Udev::Udev(string name, bool realdev) {
   fd = -1;
   eventCb = NULL;
   if (realdev) {
        char devname[80];
        this->path = name;
        open();
        devname[sizeof(devname) - 1] = '\0';
        if (ioctl(fd, EVIOCGNAME(sizeof(devname) - 1), &devname) < 1) {
            this->name = "<Unknown>";
        } else {
            this->name = (string)devname;
        }

        has_gamepad = has_event(EV_KEY) && has_keys(BTN_GAMEPAD);
        has_dpad = has_event(EV_ABS) && has_abs(ABS_HAT0X);
        has_motion = has_event(EV_ABS) && has_abs(ABS_X);
        has_mtouch = has_event(EV_ABS) && has_abs(ABS_MT_SLOT);
        close();

    } else {
        this->path = "/dev/uinput";
        this->name = name;
        has_gamepad = false;
        has_dpad = false;
        has_motion = false;
        has_mtouch = false;
        //TRY_SET(fd, open(path.c_str(), O_WRONLY | O_NONBLOCK))
        //create_virtualdev();    // implement this
    }

    LOGD("opended dev: %s (%s) %d: gpad %d dpad %d motion %d mt %d",
         path.c_str(), this->name.c_str(), fd,
         has_gamepad, has_dpad, has_motion, has_mtouch);
    event = input_event();
}

Udev::Udev(std::string path, std::string name) {
    fd = -1;
    this->path = path;
    this->name = name;
    event = input_event();
}

Udev::~Udev() {
    close();
}


// need this for constructor
#include "UdevTouch.hpp"
#include "UdevGamepad.hpp"

// only use to create real device
Udev* Udev::create(string path) {

    int fd;
    char devname[80];
    string name;

    TRY_SET(fd, libopen(path.c_str(), O_RDONLY));
    devname[sizeof(devname) - 1] = '\0';
    if (ioctl(fd, EVIOCGNAME(sizeof(devname) - 1), &devname) < 1) {
        name = "<Unknown>";
    } else {
        name = (string)devname;
    }

    bool has_gamepad = Udev::has_event(fd, EV_KEY) && Udev::has_keys(fd, BTN_GAMEPAD);
    bool has_dpad = Udev::has_event(fd, EV_ABS) && Udev::has_abs(fd, ABS_HAT0X);
    bool has_motion = Udev::has_event(fd, EV_ABS) && Udev::has_abs(fd, ABS_X);
    bool has_mtouch = Udev::has_event(fd, EV_ABS) && Udev::has_abs(fd, ABS_MT_SLOT);
    libclose(fd);

    if (has_gamepad && has_dpad && has_motion) {
        UdevGamepad* r = new UdevGamepad(name, path);
        r->has_gamepad = has_gamepad;
        r->has_dpad = has_dpad;
        r->has_motion = has_motion;
        r->has_mtouch = has_mtouch;
        return r;
    } else {
        Udev* r = new Udev(name, path);

        r->has_gamepad = has_gamepad;
        r->has_dpad = has_dpad;
        r->has_motion = has_motion;
        r->has_mtouch = has_mtouch;
        return r;
    }
}

Udev* Udev::scan_for_devname(std::string folder, std::string name) {
    DIR *dir;
    struct dirent *de;
    int fd;
    char devname[100];
    string path;
    Udev* r = NULL;

    LOGD("scan_c openind dir");
    cout << "scan_c openind dir" << endl;
    cout << folder << endl;
    cout << name << endl;
    if ((dir = opendir(folder.c_str())) != NULL) {
        while ((de = readdir(dir)) != NULL) {
            path = string(de->d_name);
            cout << "path: " << path << endl;
//            if ((devname == ".") || (devname == "..")) {
            if (path != name) {
                //LOGD("   -> ignored: %s", de->d_name);
            } else {
                path = folder + "/" + path;
                LOGD("scan_c check %s", path.c_str());
                TRY_SET(fd, libopen(path.c_str(), O_RDONLY));
                cout << "libopen: " << fd << endl;
//                //devname[sizeof(devname) - 1] = '\0';
//                if (ioctl(fd, EVIOCGNAME(sizeof(devname) - 1), &devname) > 0) {
//                    if ((string)devname == name) r = Udev::create(path);
//                }
                libclose(fd);
                r = Udev::create(path);
                break;
            }
        }
        closedir(dir);
    } else {
        LOGD(" failed to open dir");
        cerr << " failed to open dir" << endl;
    }
    return r;
}

void Udev::open() { open(O_RDONLY); }
void Udev::open(int flags) {
    if (fd<0) {
        TRY_SET(fd, libopen(path.c_str(), flags))
    }
}

void Udev::close() {
    if (fd>0) {
        libclose(fd);
        fd = -1;
    }
}

void Udev::send_event(int type, int code, int value) {
    gettimeofday(&event.time, NULL);
    event.type = (__u16)type;
    event.code = (__u16)code;
    event.value = value;

    /*TRY_CMD_MSG(write(fd, &event, sizeof(event)),
                "sendevent fd " + to_string(fd) + " t " + to_string(type) +
                " c " + to_string(code) + " v " + to_string(value));*/
    int r;
    TRY_SET(r, write(fd, &event, sizeof(event)))
    LOGD("sendevent: %s %ld - fd %d, t %d, c %d, v %d, r %d", path.c_str(),
         (long)event.time.tv_usec, fd, type, code, value, r);

}

void Udev::register_event_callback(IDevEventListener* cb) {
    eventCb = cb;
}


void Udev::poll(input_event* ev) { return poll(ev, -1); }
void Udev::poll(input_event* ev, int timeoutms) { return poll(ev, timeoutms, POLLIN); }
void Udev::poll(input_event* ev, int timeoutms, short selectevent) {
    pollfd ufd = pollfd();
    ufd.events = selectevent;
    ufd.fd = fd;
    int r = libpoll(&ufd, 1, timeoutms);
    LOGD("Poll %d %s : %d %d == %d", fd, name.c_str(), r, ufd.revents, selectevent);
    cout << "Poll " << fd << " " << name.c_str() << endl;
    if (r==0) {
        LOGD("poll %d: %s (%s) timeout after %d", fd, path.c_str(), name.c_str(), timeoutms);
        throw UdevTimeout();
    }
    else if(r>0 && (ufd.revents & selectevent) == selectevent) {
        int res = (int)read(fd, ev, sizeof(input_event));
        if(res < 0) {
            LOGD("read failed %d : %s", res, strerror(errno));
            throw UdevException("Read device " + path + " (" + to_string(fd) + ") failed: " + strerror(errno));
        }
        LOGD(" Poll: %d, t %d, c %d, v %d", (int) ev->time.tv_usec, ev->type, ev->code, ev->value);
        if (eventCb != NULL) eventCb->eventReceived(this, ev);
    }
    else {
        throw UdevException("Poll device " + path + " (" + to_string(fd) + ") failed: " + strerror(errno));
    }

}

// Returns true iff the device reports EV_KEY events.

bool Udev::has_event(int fd, uint ev) {
    unsigned long evbit = 0;
    LOGD("Try ioctl for event %d", ev);
    // Get the bit field of available event types
    if (ioctl(fd, EVIOCGBIT(0, sizeof(evbit)), &evbit) < 0) {
        LOGD(" get event fail: %s", strerror(errno));
        return 0;
    }
    return (bool)(evbit & (1 << ev));
}

bool Udev::has_event(uint ev){ return Udev::has_event(fd, ev); }

// Returns true iff the given device has |key|.
bool Udev::has_keys(int fd, uint key){
    size_t nchar = KEY_MAX/8 + 1;
    unsigned char bits[nchar];
    LOGD("Try ioctl for key %d", key);
    // Get the bit fields of available keys.
    if (ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(bits)), &bits) < 0) {
        LOGD(" get key failed: %s", strerror(errno));
        return 0;
    }
    return (bool)(bits[key/8] & (1 << (key % 8)));
}

bool Udev::has_keys(uint key){ return Udev::has_keys(fd, key); }

bool Udev::has_abs(int fd, uint code){
    size_t nchar = ABS_MAX/8 + 1;
    unsigned char bits[nchar];
    LOGD("Try ioctl for code %d", code);
    // Get the bit fields of available keys.
    if (ioctl(fd, EVIOCGBIT(EV_ABS, sizeof(bits)), &bits) < 0) {
        LOGD(" try code failed: %s", strerror(errno));
        for (int i=0; i<nchar; i++) bits[i] = 0;
    }
    return (bool)(bits[code/8] & (1 << (code % 8)));
}

bool Udev::has_abs(uint code){ return Udev::has_abs(fd, code); }


string Udev::getName() { return name; }
string Udev::getPath() { return path; }
