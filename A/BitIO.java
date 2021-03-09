package A;

import java.io.*;


public class BitIO{
	int byteArray[];		//�����Y�᪺ byte�A Ps�G�e�����]�����Y������٤p
	int byteN,bitN;			//�O���x�s�X�� byte 
	int bytePt, bitPt;		//Ū�J�ɰO���ثe��Ū�����O�ĴX�Ӧ줸�B�ĴX�줸 7->0 %8
	final int Power2[] = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 };

	public BitIO(int size) {
		byteArray=new int [size];
		for (int i=0; i<byteArray.length; i++)
			byteArray[i]=0;
		init();
	}
	public void init() {
		byteN=0;
		bitN=7;
		bytePt=0;
		bitPt=7;
	}

//==================================== Output ==================================
	public void outFile(String iofile) {
		try {
			FileOutputStream os = new FileOutputStream(iofile);
			int byteLength= byteN;
			if (bitN!=7) byteLength++;
			for (int i=0;i<byteLength ;i++)
				os.write(byteArray[i]);
			os.close();
		}
		catch (Exception e) { System.out.println(e); }
	}
	
	public void appendToBitStream(int i, int bitsLength){	// bitsLength <32
		outBits(i,bitsLength);
	}
	//�NBIT�ഫBYTE
	public void outBits(int value,int bitsLength){ //�ഫ�� Byte��J byteArray �}�C
		int i, bit;
		for (i=bitsLength-1; i>=0; i--){
			bit= (value & Power2[i])>>i;
			outBit(bit);
		}
	}
	public boolean outBit(int bit){			//�N���Y�᪺����ഫ�� Byte ��J byteArray �}�C
		if (byteN==byteArray.length) return false;
		if (bit%2==1)
			byteArray[byteN] = byteArray[byteN] | Power2[bitN];
		if (bitN==0) { bitN=7; byteN++; }
				else { bitN--; }
		return true;

	}

//==================================== Input ==================================
	public void inFile(String iofile) {
		try {
			FileInputStream is = new FileInputStream(iofile);
			int ch;
			for (byteN=0; (ch=is.read())!=-1; byteN++)
				byteArray[byteN]=ch;
			is.close();
			bytePt=0; bitPt=7; bitN=7;
		}
		catch (Exception e) { System.out.println(e); }
	}
	
	public int extractFromBitStream(int bitsLength) {
		int sum=0, bit;
		for (int i=0; i<bitsLength; i++) {
			if ((bit= inBit())!=-1) { sum= sum<<1 | bit; }
							   else { sum=-sum; break; }
		}
		return sum;
	}
	
	public int inBit() {
		if ((byteN==bytePt) && (bitPt==bitN)) return -1;	// EObits
		int r;
		if ((byteArray[bytePt] & Power2[bitPt])==0) r=0; else r=1;
		if (bitPt==0) { bitPt=7; bytePt++; }
				 else { bitPt--; }
		return r;
	}

//==================================== Other ==================================
    //�Q�i��G�i
	/*
	public String intToBitString(int x,int y){		
		int i;
		String n="";
		for (i=y-1; i>=0; i--){
			n=n+""+((int)(x/Power2[i]));
			x=(int)(x % Power2[i]);
		}
		return n;
	}*/

}

