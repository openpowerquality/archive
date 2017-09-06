
#include <msp430.h>
#include <msp430afe251.h>
#include "setup.h"

void main(void)
{
	volatile unsigned int i=0;
	WDTCTL = WDTPW + WDTHOLD;            // Stop watchdog timer. This line of code is needed at the beginning of most MSP430 projects.
	setupClock();
	setupUart();
	setupAdc24();
	_BIS_SR(LPM0_bits + GIE); // Enter LPM0 w/ interrupt
}
