package processor;

public class Memory extends OpCodeMapping {
	
	//All short variables hold 1 byte, and all int hold a short, due to arithmatic overflow on byte.
	private short A;
	private short X;
	private short Y;
	private int PC;
	private short SP;
	private short SR;
	private short[] memory = new short[0xffff];
	
	Memory() {
		//Starting instruction address
		X = 0000;
		PC = 0x0000;
		SP = 0x01ff;
		short[] tempIns = {0xe8, 0xe8, 0x8e, 0x00, 0x02, 0xee, 0x00, 0x02, 0xad, 0x00, 0x02, 0x30, 0x05, 0x4c, 0x00, 0x00, 0xa9, 0x00, 0xaa, 0x4c, 0x00, 0x00};
		/*
		INX
		INX
		STX $0200
		INC $0200
		LDA $0200
		BMI $05 (label 1)
		JMP $00
		(label 1):
			LDA #$00
			TAX
			JMP $00
		 */
		for (int i = 0; i < tempIns.length; i++) {
			memory[i] = tempIns[i];
		}
	}
	
	short getByte(int address) {
		return memory[getMirror(address)];
	}

	void setByte(int address, short value) {
		memory[getMirror(address)] = value;
	}
	
	int getTwoByte(int address) {
		address = getMirror(address);
		return (memory[address + 1] << 8) + memory[address];
	}
	
	private int getMirror(int address) {
		if (address > 0x07ff && address < 0x2000) {
			while (address > 0x07ff) address -= 0x0800;
			return address;
		}else if (address > 0x2007 && address < 0x4000) {
			while (address > 0x2007) address -= 0x40;
			return address;
		} else {
			return address;
		}
	}
	
	short getReg(Registers register) {
		switch(register) {
		case A:
			return A;
		case X:
			return X;
		case Y:
			return Y;
		case SP:
			return SP;
		case SR:
			return SR;
		default:
			return 0;
		}
	}
	
	void setReg(Registers register, short value) {
		switch(register) {
		case A:
			A = value;
			break;
		case X:
			X = value;
			break;
		case Y:
			Y = value;
			break;
		case SP:
			SP = value;
			break;
		case SR:
			SR = value;
			break;
		default:
			break;
		}
	}
	
	int getPC() { return PC; }
	
	void setPC(int value) { PC = value; }
	
}
