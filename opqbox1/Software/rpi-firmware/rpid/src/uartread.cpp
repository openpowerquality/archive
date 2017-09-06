#include "uartread.hpp"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <iostream>
#include <sys/time.h>
#include <unistd.h>
#include <boost/thread/barrier.hpp>

using namespace std;


int ttySetRaw(int fd, struct termios *prevTermios)
{
    struct termios t;

    if (tcgetattr(fd, &t) == -1)
        return -1;

    if (prevTermios != NULL)
        *prevTermios = t;

    t.c_lflag &= ~(ICANON | ISIG | IEXTEN | ECHO);
    /* Noncanonical mode, disable signals, extended
                           input processing, and echoing */

    t.c_iflag &= ~(BRKINT | ICRNL | IGNBRK | IGNCR | INLCR |
                   INPCK | ISTRIP | IXON | PARMRK);
    /* Disable special handling of CR, NL, and BREAK.
                           No 8th-bit stripping or parity error handling.
                           Disable START/STOP output flow control. */

    t.c_oflag &= ~OPOST;                /* Disable all output processing */

    t.c_cc[VMIN] = 1;                   /* Character-at-a-time input */
    t.c_cc[VTIME] = 0;                  /* with blocking */

    if (tcsetattr(fd, TCSAFLUSH, &t) == -1)
        return -1;

    return 0;
}

bool uartInit(Msp430Uart &uart)
{
    uart.fd = ::open(uart.path.c_str(),  O_RDWR | O_NOCTTY);
    if(uart.fd < 0)
    {
        cerr << "Could not open uart path" << endl;
        return false;
    }
    //ttySetRaw(uart.fd, NULL);
    return true;
}

OpqFrame* uartRead(Msp430Uart& uart, size_t len)
{
    OpqFrame* ret  = new OpqFrame;
    ret->data =std::vector<double>(len) ;
    struct timeval tv;
    gettimeofday(&tv,NULL);
    ret->timeSec = tv.tv_sec;
    ret->timeUsec = tv.tv_usec;
    ret->duration = 1;
    int index = 0;
    for(int i = 0; i< 12;)
    {
        unsigned char a;
        int readThisTime = ::read(uart.fd, &a, sizeof(unsigned char));
        if(readThisTime == 0)
        {
            boost::this_thread::interruption_point();
            continue;
        }
        i++;
    }
    while(index < len*2)
    {
        uint16_t data;
        unsigned char a;
        int readThisTime = ::read(uart.fd, &a, sizeof(unsigned char));
        if(readThisTime == 0)
        {
            boost::this_thread::interruption_point();
            continue;
        }
        if(index%2 == 0)
            data |= (uint)a;
        else
        {
            data = ((uint)a << 8);
            ret->data[index/2] = data;
        }
        index++;
    }
    return ret;
}

void uartClear(Msp430Uart &uart)
{
    fd_set rfds;
    struct timeval tv;
    int retval;
    /* Watch stdin (fd 0) to see when it has input. */
    FD_ZERO(&rfds);
    FD_SET(uart.fd, &rfds);
    /* Wait up to five seconds. */
    tv.tv_sec = 0;
    tv.tv_usec = 0;

    retval = select(1, &rfds, NULL, NULL, &tv);
    /* Don't rely on the value of tv now! */

    if (retval == -1)
        return;
    if (retval)
    {
        char a;
        ::read(uart.fd, &a, sizeof(unsigned char));
	uartClear(uart);
    }
    return;
}
