
#include <string>
#include <vector>
#include <thread>
#include <iostream> // cin cout
#include <sstream>  // istringstream
#include <dirent.h> // DIR dirent
#include <unistd.h> // usleep
#include <future>   // future/promise

#include <android/log.h>
//#include <log.h>

#define TAG "EventInj"
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


/**
 * How to debug:
 * C:\Users\ductt\AppData\Local\Android\Sdk\ndk-bundle\toolchains\aarch64-linux-android-4.9\prebuilt\windows-x86_64\bin\aarch64-linux-android-addr2line.exe -C -f -e app\src\main\assets\arm64-v8a\event_inj 000000000000
f674
 */

//#include "udevices/Udev.hpp"
//#include "udevices/UdevTouch.hpp"
//#include "udevices/UdevGamepad.hpp"
//#include "udevices/PesGamepad.hpp"
#include "udevices/Udev.cpp"
#include "udevices/UdevTouch.cpp"
#include "udevices/UdevGamepad.cpp"
#include "udevices/PesGamepad.cpp"

#include "udevices/Stoppable.cpp"

using namespace std;

vector<Udev *> pDevs;
Udev* inputDev = NULL;
promise<int> promObj;
future<int> furtObj = promObj.get_future();

StoppablePoll pollObj;
PesGamepad pes_mapper;

#define T_BACKGROUND_EXIT -1
#define T_START_POLL 1
#define T_START_MAPPING 2

#define O_POLL_EVENT 1
#define O_POLL_STOPPED 2
#define O_MAP_STOPPED 3

#define E_UNKNOWN_DEVICE 1
#define E_NO_DEVICE_FOUND 2
#define E_POLL_FAILED 3
#define E_MAP_FAILED 4
#define E_UNKNOWN_CMD 1000

int scan_devices() {
    DIR *dir;
    struct dirent *de;

    LOGD("openind dir");
    pDevs.clear();
    if ((dir = opendir("/dev/input")) != NULL) {
        while ((de = readdir(dir)) != NULL) {
            std::string devname = std::string(de->d_name);
            if ((devname == ".") || (devname == "..")) {
                //LOGD("   -> ignored: %s", de->d_name);
            } else {
                pDevs.push_back(Udev::create("/dev/input/" + devname));
            }
        }
        closedir(dir);
    } else {
        LOGD(" failed to open dir");
        return -1;
    }
    return 0;
}

int find_device_by_name(string name) {
    for (int i = 0; i < pDevs.size(); i++) {
        if (pDevs[i]->getName() == name) {
            return i;
        }
    }
    LOGD("No device found: %s", name.c_str());
    return -1;
}

void cleanup() {
    for (int i = 0; i < pDevs.size(); i++) {
        delete pDevs[i];
    }
    if (inputDev != NULL) delete inputDev;
}

void simUdevInbgr1(UdevTouchPoint *tp) {
    tp->active(900, 1400);
    for (int i = 0; i < 30; i++) {
        usleep(100 * 1000);
        tp->rmove(-10, -10);
    }
    for (int i = 0; i < 20; i++) {
        usleep(100 * 1000);
        tp->rmove(0, 10);
    }
    tp->release();
}

void simUdevInbgr2(UdevTouchPoint *tp) {
    tp->active(400, 700);
    for (int i = 0; i < 30; i++) {
        usleep(100 * 1000);
        tp->rmove(10, 10);
    }
    tp->rswipe(200, -200, 500);
    for (int i = 0; i < 20; i++) {
        usleep(100 * 1000);
        tp->rmove(-10, 0);
    }
    tp->release();
}

void simUdev() {
    /*
    sleep(3);   // seconds
    virmt->open();
    thread t1(simUdevInbgr1, virmt->new_touch());
    sleep(2);
    thread t2(simUdevInbgr2, virmt->new_touch());
    t1.join();
    t2.join();
    virmt->close();
    //delete &ud;
     */
}

void backgroud_task() {
    int todo;
    LOGD("Background task is running");
    while (1) {
        todo = furtObj.get();
        LOGD("Background task receied: %d", todo);

        if (todo == T_BACKGROUND_EXIT) {
            break;
        }

        else if (todo == T_START_POLL) {
            //int iPollid = furtObj.get();
            LOGD("BGR: received start polling:");
            //Udev *sdev = pDevs[iPollid];
            UdevEventLogging eventcb = UdevEventLogging();
            //pollObj.set_device(pDevs[iPollid])->set_callback(eventcb);
            pollObj.set_device(inputDev)->set_callback(&eventcb);
            try {
                pollObj.start();
            } catch (exception e) {
                LOGD("Poll failed: %s", e.what());
                cerr << E_POLL_FAILED << endl;
            }
            cout << O_POLL_STOPPED << endl;
        }

        else if (todo == T_START_MAPPING) {
            //int iMapid = furtObj.get();
            LOGD("BGR: received start mapping:");
            //pes_mapper.set_device(pDevs[iMapid]);
            pes_mapper.set_device(inputDev);
            try {
                pes_mapper.start();
            } catch (exception e) {
                LOGD("Map failed: %s", e.what());
                cerr << E_MAP_FAILED << endl;
            }

            cout << O_MAP_STOPPED << endl;
        }

    }
    LOGD("background exited");
}

void stop_all() {
    if (pollObj.isRunning()) pollObj.stop();
    if (pes_mapper.isRunning()) pes_mapper.stop();
}

int main(int argc, char *argv[]) {
    LOGD("event_inj started");
    cout << "event_inj start output" << endl;
    cerr << "event_inj start error" << endl;

    /*
    if (scan_devices() < 0) {
        cerr << E_NO_DEVICE_FOUND << endl;
    }
    LOGD("Devices found: %d", (int) pDevs.size());
    for (int i = 0; i < pDevs.size(); i++) {
        LOGD("  [%d] %s (%s)", i, pDevs[i]->getPath().c_str(), pDevs[i]->getName().c_str());
    }
     */

    // background handler;
    thread bgr_th(backgroud_task);

    string userin;
    ios::sync_with_stdio(false);    // only use c++ IO, dont slow down
    do {
        getline(cin, userin);
        LOGD("uerinput: %s", userin.c_str());
        cout << "input" << userin.c_str() << endl;
        istringstream iss(userin);
        vector<string> cmds{istream_iterator<string>{iss},
                            istream_iterator<string>{}};

        if (cmds[0] == "exit_inj") {
            LOGD(">> exit command");
            //bgr_th.terminate();
            stop_all();
            promObj.set_value(T_BACKGROUND_EXIT);
            break;
        } else if (cmds[0] == "rescan") {
            LOGD(">> rescan command");
        } else if (cmds[0] == "pollonly") {
            LOGD(">> pollonly command");
            cout << "pollonly cmd " << cmds[1] << endl;
            stop_all();
            //int d = find_device_by_name(cmds[1]);
            inputDev = Udev::scan_for_devname("/dev/input", cmds[1]);
            if (inputDev == NULL) {
                cerr << E_UNKNOWN_DEVICE << endl;
            } else {
                promObj.set_value(T_START_POLL);
                //promObj.set_value(d);
            }
        } else if (cmds[0] == "start") {
            LOGD(">> start command");
            stop_all();
            //int d = find_device_by_name(cmds[1]);
            inputDev = Udev::scan_for_devname("/dev/input", cmds[1]);
            if (inputDev == NULL) {
                cerr << E_UNKNOWN_DEVICE << endl;
            } else {
                promObj.set_value(T_START_MAPPING);
                //promObj.set_value(d);
            }
        } else if (cmds[0] == "stop_inj") {
            LOGD(">> stop command");
            stop_all();
        } else if (cmds[0] == "udev") {
            LOGD(">> udev command");
            simUdev();
        } else {
            LOGD(">> Unknow cmd: %s", cmds[0].c_str());
            cerr << E_UNKNOWN_CMD << endl;
        }

    } while (true);


    bgr_th.join();
    cleanup();

    LOGD("EXITING");
    //cout.flush();

    return EXIT_SUCCESS;
}
