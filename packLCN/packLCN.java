package packLCN;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class packLCN {

	/**
	 * @param args
	 */
	public static int[] readfile(int pos, int len) {
		byte src[] = new byte[len];
		int read = 0;
		try {
			os.seek(pos);

			read = os.read(src, 0, len);
		} catch (IOException e) {
			System.out.println("IOException:");
			e.printStackTrace();
		}
		byte r1[] = new byte[read];
		int[] i_src = new int[read];
		for (int i = 0; i < read; i++)
			i_src[i] = src[i] < 0 ? src[i] + 256 : src[i];

		return i_src;

	}

	private static int getInt(int[] b) {
		int ret = ((b[3] * 256 + b[2]) * 256 + b[1]) * 256 + b[0];
		return ret;
	}

	public static void main(String[] args) throws IOException {
		int hexWords = 0xCAFEBABE;
		String FILENAME = "data/lcn_image.dat";
		String FILENAME1 = "data/LCN_IMAGE_NEW.DAT";
		String FNAVI_BIF = "data/NAVI_BIF.bin";
		File dir1 = new File(".");
		System.out.println("Current dir : " + dir1.getCanonicalPath());
		InputStream in = new FileInputStream(FILENAME);

		OutputStream out = new FileOutputStream(FILENAME1);

		OutputStream nav = new FileOutputStream(FNAVI_BIF);

		byte[] buf = new byte[1024];

		int len;

		while ((len = in.read(buf)) > 0) {

			out.write(buf, 0, len);

		}

		in.close();

		out.close();

		// ByteBuffer buffer;

		os = new RandomAccessFile(FILENAME, "r");
		os1 = new RandomAccessFile(FILENAME1, "rw");
		/*
		 * FileChannel os = raf.getChannel(); DataInputStream os = new
		 * DataInputStream(new FileInputStream( FILENAME));
		 */

		byte r[] = new byte[48];
		int b[];
		int offs = 0;
		// b = read(os,offs,4)
		b = readfile(offs, 4);
		offs += 4;

		int picsOffs = getInt(b);
		int picsCnt = (picsOffs - 4) / 64;
		System.out.println("PisOffs " + picsOffs + " picsCnt " + picsCnt);

		int picOffs[] = new int[picsCnt];
		int picLen[] = new int[picsCnt];
		int picX[] = new int[picsCnt];
		int picY[] = new int[picsCnt];
		String picName[] = new String[picsCnt];
		String picNamei[] = new String[picsCnt];
		int i = 0;
		while (i < picsCnt) {
			b = readfile(offs, 4);
			offs += 4;
			picOffs[i] = getInt(b);

			b = readfile(offs, 4);
			offs += 4;
			picLen[i] = getInt(b);
			b = readfile(offs, 4);
			offs += 4;
			picX[i] = getInt(b);
			b = readfile(offs, 4);
			offs += 4;
			picY[i] = getInt(b);
			try {
				os.seek(offs);
				os.read(r, 0, 48);
				picName[i] = new String(r, "MS949"); /* MS949 EUC_KR ISO2022KR */
				picNamei[i] = new String(r, "MS949");
				picName[i] = "files_out/".concat(
						picName[i].substring(0, picName[i].indexOf("\0")))
						.concat(".bmp");
				picNamei[i] = "files_in/".concat(
						picNamei[i].substring(0, picNamei[i].indexOf("\0")))
						.concat(".bmp");
			} catch (IOException e) {
				System.out.println("IOException:");
				e.printStackTrace();
			}
			float x = picX[i];
			x = Math.round(x / 2) * 4 * picY[i] + bmpHeader.length + 2;
			byte[] bmp = new byte[(int) x];

			System.arraycopy(bmpHeader, 0, bmp, 0, bmpHeader.length);

			int inb = bmp.length;
			// filesize
			bmp[2] = (byte) ((inb) & 0xFF);
			bmp[3] = (byte) ((inb >>> 8) & 0xFF);
			bmp[4] = (byte) ((inb >>> 16) & 0xFF);
			bmp[5] = (byte) ((inb >>> 24) & 0xFF);

			// picWidth
			inb = picX[i];
			bmp[18] = (byte) ((inb) & 0xFF);
			bmp[19] = (byte) ((inb >>> 8) & 0xFF);
			bmp[20] = (byte) ((inb >>> 16) & 0xFF);
			bmp[21] = (byte) ((inb >>> 24) & 0xFF);

			// picHeight
			inb = picY[i];
			bmp[22] = (byte) ((inb) & 0xFF);
			bmp[23] = (byte) ((inb >>> 8) & 0xFF);
			bmp[24] = (byte) ((inb >>> 16) & 0xFF);
			bmp[25] = (byte) ((inb >>> 24) & 0xFF);

			try {
				os.seek(picOffs[i]);

				int oofs = bmpHeader.length;
				int j = 0;
				/*
				 * System.out.println("n=" + i + " PicOff=" + picOffs[i] +
				 * " picLen=" + picLen[i] + " picX=" + picX[i] + " picY=" +
				 * picY[i] + " picName='" + picName[i]);
				 */
				while (j < picY[i]) {
					byte[] bmpx = new byte[picX[i] * 2];
					os.read(bmpx, 0, picX[i] * 2);
					for (int n = 0; n < picX[i]; n++) {
						// System.out.println("x="+picX[i]+" n="+n);
						bmp[oofs + n * 2] = bmpx[(picX[i] - n - 1) * 2];
						bmp[oofs + n * 2 + 1] = bmpx[(picX[i] - n - 1) * 2 + 1];
					}
					// System.out.println("offs=" + oofs);
					j++;
					x = picX[i];
					x = Math.round(x / 2) * 4;
					oofs += (int) x;
				}

			} catch (IOException e) {
				System.out.println("IOException:");
				e.printStackTrace();
			}
			// if (i==29) {
			DataOutputStream bmp_out = new DataOutputStream(
					new FileOutputStream(picName[i]));
			// os.writeInt(hexWords);
			// System.out.println("bmp.length=" + bmp.length);
			bmp_out.write(bmp, 0, bmp.length);
			bmp_out.close();
			// если картинка новаЯ существует
			File new_bmp = new File(picNamei[i]);
			File old_bmp = new File(picName[i]);
			if (new_bmp.exists()) {
				System.out.print("New picture " + picNamei[i] + " exists.");
				if (new_bmp.length() == old_bmp.length()) {
					// если размеры картинок старой и новой совпадают
					System.out.print("... size is correct\n");
					if (picNamei[i].equalsIgnoreCase("files_in/BG_INTRO.bmp"))
					{	
						navo = new RandomAccessFile(FNAVI_BIF, "rw");
					}

					RandomAccessFile bmp_new = new RandomAccessFile(
							new_bmp, "r");
					bmp_new.seek(bmpHeader.length);

					System.out.println(" x="+picX[i]+" y="+picY[i]+" offs="+picOffs[i]);
					int j = 0;
					int oofs = bmpHeader.length;
					while (j < picY[i]) {
						//System.out.println("j="+j+" ("+oofs+") ");
						byte[] by = new byte[1];
						for (int n = 0; n < picX[i]; n++) {
							//System.out.print(" ;"+n);
							//bmp_new.seek(oofs + n * 2);
							bmp_new.seek(oofs + n * 2);
							bmp_new.read(by, 0, 1);
							//System.out.println(by);
							os1.seek(picOffs[i] + (j*picX[i]*2)+(picX[i] - n - 1) * 2);
							os1.write(by, 0, 1);
							if (picNamei[i].equalsIgnoreCase("files_in/BG_INTRO.bmp"))
							{	
								navo.seek((j*picX[i]*2)+(picX[i] - n - 1) * 2);
								navo.write(by, 0, 1);
							}
							
							bmp_new.seek(oofs + n * 2+1);
							bmp_new.read(by, 0, 1);
							

							os1.seek(picOffs[i] + (j*picX[i]*2)+(picX[i] - n - 1) * 2 + 1);
							os1.write(by, 0, 1);

							if (picNamei[i].equalsIgnoreCase("files_in/BG_INTRO.bmp"))
							{	
								navo.seek( (j*picX[i]*2)+(picX[i] - n - 1) * 2 + 1);
								navo.write(by, 0, 1);
							}

							//System.out.print(by);
						}


						j++;
						x = picX[i];
						x = Math.round(x / 2) * 4;
					//	System.out.println("\nx="+x+"");
						oofs += (int) x;
					}
					if (picNamei[i].equalsIgnoreCase("files_in/BG_INTRO.bmp"))
					{	
						navo.close();
					}
					bmp_new.close();
					//System.out.println(picOffs[i] + (picX[i] ) * 2);
				} else
					System.out.print("... by size incorrect.\n");

			}

			// }
			offs += 48;
			// System.out.println("i="+i+" PicOff=" + picOffs[i] + " picLen=" +
			// picLen[i] +
			// " picX="+picX[i]+" picY="+picY[i]+" picName='"+picName[i]+"' lenPicName="+picName[i].indexOf("\0"));
			i++;
		}

		// os.writeDouble(d);
		os.close();
		os1.close();
		System.out.print("Done...\n");
	}

	public static RandomAccessFile os;
	public static RandomAccessFile os1;
	public static RandomAccessFile navo;
	private static byte[] bmpHeader = { 0x42, 0x4d,
			// filesize
			0, 0, 0, 0,
			// reserved
			0, 0, 0, 0,
			// offset
			0x46, 0, 0, 0,
			// header size
			0x38, 0, 0, 0,
			// picWidth
			0, 0, 0, 0,
			// picHeight
			0, 0, 0, 0,
			// planes bitperpixel
			0x01, 0, 0x10, 0,
			// compression
			0x03, 0,
			0,
			0, // BI_ALPHABITFIELDS
				// imagesize
			0, 0, 0, 0,
			// x pixel per meter
			0x12, 0x0b, 0, 0,
			// y pixel per meter
			0x12, 0x0b, 0, 0,
			// biClrUsed
			0, 0, 0, 0,
			// biClrImportant?
			0, 0, 0, 0,
			// palette
			0, (byte) 0xf8, 0, 0, (byte) 0xe0, (byte) 0x07, 0, 0, (byte) 0x1f,
			0, 0, 0, 0, 0, 0, 0

	};

}
