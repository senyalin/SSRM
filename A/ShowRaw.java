package A;

import java.awt.*; 
import java.awt.event.*;
import java.awt.image.*; 
import java.io.*; 

@SuppressWarnings("serial")
class ShowRaw extends Frame { 
		int[] p= new int[512*512]; 
		Image ImageObj; 
		int   inTop, inLeft, ch, i, j; 
		/*public static void main(String[] args) { 
			ShowRaw obj= new ShowRaw("RawImage\\Lena.raw",512,512,"Raw Image Testing"); 
			obj.repaint(); 
		}*/
		public ShowRaw(String sFname,int imgWidth,int imgHeight,String sTitle) { 
			try
			{
				InputStream is = new FileInputStream(sFname); 
				for (i = 0 ; i < imgHeight ; i++ ) // Read the input image file 
					for (j = 0 ; j < imgWidth ; j++ )
						if ((ch=is.read())!=-1) 
							p[i*512+j]= ch | ch<<8 | ch<<16 | 0xFF000000;  // LRGB 
				ImageObj = createImage(new MemoryImageSource(imgWidth,imgHeight, p, 0, 512));
				setVisible(true); 
				inTop=  getInsets().top; 
				inLeft= getInsets().left;
				setSize(inLeft+imgWidth, inTop+imgHeight); 
				setTitle(sTitle); 
				addWindowListener(new WindowAdapter() 
								{ public void windowClosing(WindowEvent e) 
												{ System.exit(0); }    }); 
				}
			catch (Exception e) { System.out.println(e); }
		}// end of constructor 
		public void paint(Graphics g) { 
			if (ImageObj != null) g.drawImage(ImageObj,inLeft,inTop,this); 
		} 
}