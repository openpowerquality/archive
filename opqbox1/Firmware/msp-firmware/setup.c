/*
 * init_clock.c
 *
 *  Created on: Feb 26, 2014
 *      Author: tusk
 */

#include <msp430afe251.h>

#include "setup.h"
void setupClock()
{
	volatile unsigned int i=0;
	BCSCTL1 &= ~XT2OFF;                       // Activate XT2 high freq xtal
	//BCSCTL1 &= ~XTS;						  //High frequency mode

	BCSCTL3 |= XT2S_2+LFXT1S_2;               // 12MHz crystal or resonator
	do
	{
		IFG1 &= ~OFIFG;                         // Clear OSCFault flags
		for (i = 0xFFF; i > 0; i--);            // Time for flag to set
	}
	while (IFG1 & OFIFG);                     // OSCFault flag still set?
	BCSCTL2 |= SELS+SELM_2;                   // MCLK = XT2 HF XTAL (safe)
}

void setupUart()
{

	/////////////init usart as uart mode////////////////
	P1SEL |= BIT3+BIT4;                      	// P1.3,1.4 = USART0 TXD/RXD
	U0CTL = CHAR;								// 8-bit, UART
	U0TCTL = SSEL1 + SSEL0;
	ME1 |= UTXE0 + URXE0;						// Module enable

	UBR00=0x68;
	UBR10=0x00;
	UMCTL0=0x04;

	UCTL0 &= ~SWRST;							// Enable usart
	IE1 |= URXIE0;             					 // Enable USART0 RX interrupt
	//sets up the data ready flag
	P1DIR&=~BIT2; //set as input
	P1SEL&=~BIT2;
	P1SEL2&=~BIT2;
	P1REN |=BIT2;
}

void setupAdc24()
{
	unsigned int i;
	SD24CTL = SD24REFON + SD24SSEL0 + SD24DIV1 + SD24XDIV0;          // 1.2V ref, MCLK divide by 12

	SD24CCTL0 = SD24LSBTOG;

	SD24INCTL0 = SD24INTDLY0;                // Interrupt on 3rd sample
	SD24CCTL0 |= SD24IE ;                     // Enable interrupt
	for (i = 0; i < 0x3600; i++);             // Delay for 1.2V ref startup

	SD24CCTL0 |= SD24SC;                      // Set bit to start conversion
}
