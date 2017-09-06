#include <msp430afe251.h>
#pragma vector=USART0RX_VECTOR
__interrupt void USART0_RX (void)
{
	if ((P1IN & BIT2) != 0x00)
		while (!(IFG1 & UTXIFG0));                // USART0 TX buffer ready?
			TXBUF0 = RXBUF0;                          // RXBUF0 to TXBUF0
}

#pragma vector=SD24_VECTOR
__interrupt void SD24AISR(void)
{
	switch (SD24IV)
	{
	case 2:                                   // SD24MEM Overflow
		break;
	case 4:
	{										  // SD24MEM0 IFG
		unsigned short  high  = SD24MEM0;
		unsigned short  low  = SD24MEM0;
		if ((P1IN & BIT2) == 0x00)
		{
			while (!(IFG1 & UTXIFG0));                // USART0 TX buffer ready?
			TXBUF0 = high >> 8;
			while (!(IFG1 & UTXIFG0));                // USART0 TX buffer ready?
			TXBUF0 = high;
		}
	}
	break;
	case 6:                                   // SD24MEM1 IFG
		break;
	case 8:                                   // SD24MEM2 IFG
		break;
	}
}
