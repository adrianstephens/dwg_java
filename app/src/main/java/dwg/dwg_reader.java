package dwg;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.lang.ref.SoftReference;
import java.lang.reflect.*;

class instantiator {
	public static boolean does_implement(Class<?> type, Class<?> check) {
		for (var i : type.getInterfaces()) {
			if (i == check)
				return true;
		}
		var	sup = type.getSuperclass();
		return sup != null && sup != Object.class && does_implement(sup, check);
	}

	public static void instantiate_fields(Object obj, Class<?> check) {
		for (var otype = obj.getClass(); otype != Object.class; otype = otype.getSuperclass()) {
			for (var field : otype.getDeclaredFields()) {
				var	ftype = field.getType();
				if (!Modifier.isStatic(field.getModifiers()) && does_implement(ftype, check)) {
					try {
						field.set(obj, ftype.getDeclaredConstructor().newInstance());
					} catch (IllegalArgumentException | IllegalAccessException | InstantiationException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
};

class CRC16 implements Checksum { 
	static int[] table = {
		0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
		0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
		0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
		0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
		0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
		0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
		0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
		0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
		0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
		0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
		0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
		0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
		0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
		0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
		0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
		0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
		0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
		0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
		0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
		0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
		0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
		0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
		0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
		0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
		0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
		0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
		0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
		0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
		0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
		0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
		0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
		0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
	};
	int	crc;

	CRC16()						{ crc = 0; }
	CRC16(int crc)				{ this.crc = crc; }
 	public void reset() 		{ crc = 0; }
  	public long getValue() 		{ return crc; }
  	public void update(int b) 	{ crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff]; }
  	public void update(byte[] b, int off, int len) {
		for (int i = off, e = i + len; i != e; ++i)
			crc = (crc >>> 8) ^ table[(crc ^ b[i]) & 0xff];
  	}
  
}

// must supply
// 	public int 		getc();
//	public int		remaining();
//	public long		tell();
//	public void		seek(long offset);
//	public int 		tell_bit();
//	public void		seek_bit(int offset);

interface reader {
	public int getc();
	public long remaining();
	public long tell();
	public void seek(long offset);

	public default boolean eof() {
		return remaining() <= 0;
	}

	public default void seek_cur(long offset) {
		seek(tell() + offset);
	}

	public default int readbuff(byte[] bytes, int offset, int count) {
		for (int i = 0; i < count; i++)
			bytes[offset + i] = (byte) getc();
		return count;
	}

	public default byte[] readbuff(int n) {
		byte[] bytes = new byte[n];
		readbuff(bytes, 0, n);
		return bytes;
	}

	public default int get16() 			{ return getc() | (getc() << 8); }
	public default int get32() 			{ return get16() | (get16() << 16); }
	public default long get64() 		{ return get32() | ((long)get32() << 32); }
	public default double getDouble() 	{ return ByteBuffer.wrap(readbuff(8)).getDouble(); }

	public default boolean read(short[] array) {
		for (int i = 0; i < array.length; i++)
			array[i] = (short)get16();
		return true;
	}

	public default boolean read(char[] array) {
		for (int i = 0; i < array.length; i++)
			array[i] = (char) get16();
		return true;
	}
	public default boolean read(int[] array) {
		for (int i = 0; i < array.length; i++)
			array[i] = get32();
		return true;
	}

	public default boolean read(long[] array) {
		for (int i = 0; i < array.length; i++)
			array[i] = get64();
		return true;
	}

	// readables

	public default <T extends readable> boolean read(T t) {
		return t.read(this);
	}

	public default <T extends readable> boolean read(T[] array) {
		var	type = array.getClass().getComponentType();
		try {
			Constructor<?>	cons = type.getDeclaredConstructor();
			for (int i = 0; i < array.length; i++) {
				array[i] = (T)cons.newInstance();
				if (!array[i].read(this))
					return false;
			}
			return true;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException  e) {
			return false;
		}
	}

	public default boolean read(readable t0, readable... tt) {
		boolean b = t0.read(this);
		for (readable t : tt)
			b = b && t.read(this);
		return b;
	}
};

interface readable {
	public boolean read(reader r);
};

enum VER {
    BAD(0),
    R13(1012),
    R14(1014),
    R2000(1015),
    R2004(1018),
    R2007(1021),
    R2010(1024),
    R2013(1027),
    R2018(1032),
    MIN_VER(R13),
    MAX_VER(R2018);

    public final int value;

    private VER(int value) {
        this.value = value;
    }

    private VER(VER v) {
        this.value = v.value;
    }
	
	public static VER FromInt(int id) {
		for (var e : values()) {
			if (e.value == id)
				return e;
		}
		return BAD;
	}
};

// must additionally supply
//	public int 		ver(VER v);
//	public boolean 	get_bit();
// 	public boolean 	get_bits();
//	public int 		tell_bit();
//	public void		seek_bit(int offset);

interface bit_reader extends reader {
    public int 		ver(VER v);
    public boolean 	get_bit();
    public int 		get_bits(int n);
	public int 		tell_bit();
	public void 	seek_bit(int offset);

	public default void seek_cur_bit(int offset) {
		seek_bit(tell_bit() + offset);
	}

	public default int with_flag(int value) {
		return get_bit() ? value : 0;
	}
	
	public default <T extends bit_readable> boolean read(T t) {
		return t.read(this);
	}

	public default <T extends bit_readable> boolean read(T[] array) {
		var	type = array.getClass().getComponentType();
		try {
			Constructor<?>	cons = type.getDeclaredConstructor();
			for (int i = 0; i < array.length; i++) {
				array[i] = (T)cons.newInstance();
				if (!array[i].read(this))
					return false;
			}
			return true;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException  e) {
			return false;
		}
	}

	public default boolean read(bit_readable t0, bit_readable... tt) {
		boolean b = t0.read(this);
		for (var t : tt)
			b = b && t.read(this);
		return b;
	}
};

interface bit_readable {
	static void instantiate(Object obj) { instantiator.instantiate_fields(obj, bit_readable.class); }
    public boolean read(bit_reader r);
};
 
//-----------------------------------------------------------------------------
// reader implementations
//-----------------------------------------------------------------------------

class SubReader implements reader {
	reader b;
	long	offset, length;

	SubReader(reader _b, long _offset, long _length) {
		b = _b;
		offset = _offset;
		length = _length;
		b.seek(offset);
	}

	public long remaining() 		{ return offset + length - b.tell(); }
	public long tell() 				{ return b.tell() - offset; }
	public void seek(long offset) 	{ b.seek(offset + this.offset); }
	public int getc() 				{ return b.getc(); }	
	public int readbuff(byte[] bytes, int offset, int count) {
		return b.readbuff(bytes, offset, count);
	}
};

class FileReader implements reader {
	RandomAccessFile 	b;
	long		length;

	FileReader(RandomAccessFile _b) { b = _b; }

	public long remaining() {
		try {
			return b.length() - b.getFilePointer();
		} catch (IOException e) {
			return 0;
		}
	}
	public long tell() {
		try {
			return b.getFilePointer();
		} catch (IOException e) {
			return 0;
		}
	}
	public void seek(long offset) {
		try {
			b.seek(offset);
		} catch (IOException e) {
		}
	}
	public int getc() {
		 try {
			return b.readByte() & 0xff;
		} catch (IOException e) {
			return -1;
		}
	}
};

class MemoryReader implements reader {
	byte[] b;
	int p = 0;

	MemoryReader(byte[] _b) {
		b = _b;
	}

	public static MemoryReader wrap(byte[] b) { return new MemoryReader(b); }
	public MemoryReader at(int offset) { seek(offset); return this; }

	public long remaining() 		{ return b.length - p;	}
	public long tell() 				{ return (long)p;	}
	public void seek(long offset)	{ p = Math.min((int)offset, b.length);	}
	public int getc() 				{ return remaining() == 0 ? -1 : b[p++] & 0xff; }
};

// -----------------------------------------------------------------------------
// bit_reader implementations
// -----------------------------------------------------------------------------

class memory_bits_reader {
    byte[]  b;
	int     p;
	int	    bit;

	memory_bits_reader(memory_bits_reader x) 	{ b = x.b; p = x.p; bit = x.bit; }
	memory_bits_reader(byte[] _b)				{ b = _b; p = 0; bit = 0; }
	public long		remaining()					{ return b.length - p - (bit > 0 ? 1 : 0); }
	public long		tell()						{ return p + (bit > 0 ? 1 : 0); }
	public void		seek(long offset)			{ p = Math.min((int)offset, b.length); bit = 0; }
	public int 		tell_bit()					{ return p * 8 + bit; }
	public void		seek_bit(int offset)		{ p = Math.min(offset / 8, b.length); bit = offset & 7; }
	public void		seek_cur_bit(int offset)	{ seek_bit(tell_bit() + offset); }

	public int		getc() {
		if (remaining() == 0)
			return -1;
		++p;
		return bit == 0
			? (b[p - 1] + 256) & 255
			: ((((b[p-1] & 0xff) << 8) | (b[p] & 0xff)) >> (8 - bit)) & 0xff;
	}

	public boolean get_bit() {
		boolean	ret = ((b[p] >> (7 - bit)) & 1) != 0;
		bit = (bit + 1) & 7;
        if (bit == 0)
			++p;
		return ret;
	}
	
	public int get_bits(int n) {
		bit += n;

		int	ret = bit <= 8
			? (b[p] >> (8 - bit))
			: (b[p] << (bit - 8)) | ((b[p + 1] & 0xff) >> (16 - bit));

		p += bit >> 3;
		bit &= 7;

		return ret & ((1 << n) - 1);
	}
};


class bitsin extends memory_bits_reader implements bit_reader {
	VER		ver;
	int		size = 0;

	bitsin(byte[] b, VER v) { super(b); ver = v; }
	bitsin(bitsin b) { super((memory_bits_reader)b); ver = b.ver; size = b.size; }
    	
    public int ver(VER v) {
        return ver.compareTo(v);
    }
};


class bitsin2 extends bitsin {
	bitsin	sbits;
	int soffset = 0;

	bitsin2(bitsin2 bits) {
		super(bits);
		sbits = bits.sbits;
	}
	bitsin2(bitsin bits)	{
		 super(bits);
		 sbits = bits;
	}
	bitsin2(bitsin bits, bitsin _sbits, int _soffset)	{
		super(bits);
		sbits = _sbits;
		soffset = _soffset;
		sbits.seek_bit(soffset);
	 }

	boolean	check_skip_strings() {
		if (soffset != 0 && tell_bit() != soffset)
			return false;
		if (this != sbits) {
			return sbits.tell_bit() == size - 17;
		}
		return sbits.tell_bit() == size;
	}
};


class bitsin3 extends bitsin2 {
	bitsin	hbits;
	bitsin3(bitsin2 bits) {
		super(bits);
		hbits = bits;
	}
	bitsin3(bitsin2 bits, bitsin _hbits) {
		super(bits);
		hbits = _hbits;

	}
};

class bit_seeker implements AutoCloseable {
	bitsin  bits;
	int	    end;
	
    public bit_seeker(bitsin _bits, int size) {
        bits = _bits;
        end = bits.tell_bit() + size;
    }
	public void close() { bits.seek_bit(end); }
};

//-----------------------------------------------------------------------------
//	class DWG
// -----------------------------------------------------------------------------

class DWG {

enum DXFCODE {
	DXF_STRING		(1000),
	DXF_INVALID		(1001),
	DXF_BRACKET		(1002),
	DXF_LAYER_REF	(1003),
	DXF_BINARY		(1004),
	DXF_ENTITY_REF	(1005),
	DXF_POINTS		(1010),
	DXF_REALS		(1040),
	DXF_SHORT		(1070),
	DXF_LONG		(1071);

    public final int value;
    private DXFCODE(int value) { this.value = value; }
};

enum OBJECTTYPE {
	UNUSED					(0x00, null),
	TEXT					(0x01, DRW_TEXT.class),					//	E
	ATTRIB					(0x02, DRW_ATTRIB.class),				//	E
	ATTDEF					(0x03, DRW_ATTDEF.class),				//	E
	BLOCK					(0x04, DRW_BLOCK.class),				//	E
	ENDBLK					(0x05, DRW_ENDBLK.class),				//	E
	SEQEND					(0x06, DRW_SEQEND.class),				//	E
	INSERT					(0x07, DRW_INSERT.class),				//	E
	MINSERT					(0x08, DRW_MINSERT.class),				//	E
	VERTEX_2D				(0x0A, DRW_VERTEX_2D.class),			//	E
	VERTEX_3D				(0x0B, DRW_VERTEX_3D.class),			//	E
	VERTEX_MESH				(0x0C, DRW_VERTEX_MESH.class),			//	E
	VERTEX_PFACE			(0x0D, DRW_VERTEX_PFACE.class),			//	E
	VERTEX_PFACE_FACE		(0x0E, DRW_VERTEX_PFACE_FACE.class),	//	E
	POLYLINE_2D				(0x0F, DRW_POLYLINE_2D.class),			//	E
	POLYLINE_3D				(0x10, DRW_POLYLINE_3D.class),			//	E
	ARC						(0x11, DRW_ARC.class),					//	E
	CIRCLE					(0x12, DRW_CIRCLE.class),				//	E
	LINE					(0x13, DRW_LINE.class),					//	E
	DIMENSION_ORDINATE		(0x14, DRW_DIMENSION_ORDINATE.class),	//	E
	DIMENSION_LINEAR		(0x15, DRW_DIMENSION_LINEAR.class),		//	E
	DIMENSION_ALIGNED		(0x16, DRW_DIMENSION_ALIGNED.class),	//	E
	DIMENSION_ANG_PT3		(0x17, DRW_DIMENSION_ANG_PT3.class),	//	E
	DIMENSION_ANG_LN2		(0x18, DRW_DIMENSION_ANG_LN2.class),	//	E
	DIMENSION_RADIUS		(0x19, DRW_DIMENSION_RADIUS.class),		//	E
	DIMENSION_DIAMETER		(0x1A, DRW_DIMENSION_DIAMETER.class),	//	E
	POINT					(0x1B, DRW_POINT.class),				//	E
	FACE_3D					(0x1C, DRW_FACE_3D.class),				//	E
	POLYLINE_PFACE			(0x1D, DRW_POLYLINE_PFACE.class),		//	E
	POLYLINE_MESH			(0x1E, DRW_POLYLINE_MESH.class),		//	e
	SOLID					(0x1F, DRW_SOLID.class),				//	E
	TRACE					(0x20, DRW_TRACE.class),				//	E
	SHAPE					(0x21, DRW_SHAPE.class),				//	e
	VIEWPORT				(0x22, DRW_VIEWPORT.class),				//	E
	ELLIPSE					(0x23, DRW_ELLIPSE.class),				//	E
	SPLINE					(0x24, DRW_SPLINE.class),				//	E
	REGION					(0x25, DRW_REGION.class),				//	e
	SOLID_3D				(0x26, DRW_SOLID_3D.class),				//	e
	BODY					(0x27, DRW_BODY.class),					//	e
	RAY						(0x28, DRW_RAY.class),					//	E
	XLINE					(0x29, DRW_XLINE.class),				//	E
	DICTIONARY				(0x2A, DRW_DICTIONARY.class),			//	O
	OLEFRAME				(0x2B, DRW_OLEFRAME.class),				//	e
	MTEXT					(0x2C, DRW_MTEXT.class),				//	E
	LEADER					(0x2D, DRW_LEADER.class),				//	E
	TOLERANCE				(0x2E, DRW_TOLERANCE.class),			//	e
	MLINE					(0x2F, DRW_MLINE.class),				//	E
	BLOCK_CONTROL_OBJ		(0x30, DRW_BLOCK_CONTROL_OBJ.class),
	BLOCK_HEADER			(0x31, DRW_BLOCK_HEADER.class),
	LAYER_CONTROL_OBJ		(0x32, DRW_LAYER_CONTROL_OBJ.class),
	LAYER					(0x33, DRW_LAYER.class),
	STYLE_CONTROL_OBJ		(0x34, DRW_STYLE_CONTROL_OBJ.class),
	STYLE					(0x35, DRW_STYLE.class),
	LTYPE_CONTROL_OBJ		(0x38, DRW_LTYPE_CONTROL_OBJ.class),
	LTYPE					(0x39, DRW_LTYPE.class),
	VIEW_CONTROL_OBJ		(0x3C, DRW_VIEW_CONTROL_OBJ.class),
	VIEW					(0x3D, DRW_VIEW.class),
	UCS_CONTROL_OBJ			(0x3E, DRW_UCS_CONTROL_OBJ.class),
	UCS						(0x3F, DRW_UCS.class),
	VPORT_CONTROL_OBJ		(0x40, DRW_VPORT_CONTROL_OBJ.class),
	VPORT					(0x41, DRW_VPORT.class),
	APPID_CONTROL_OBJ		(0x42, DRW_APPID_CONTROL_OBJ.class),
	APPID					(0x43, DRW_APPID.class),
	DIMSTYLE_CONTROL_OBJ	(0x44, DRW_DIMSTYLE_CONTROL_OBJ.class),
	DIMSTYLE				(0x45, DRW_DIMSTYLE.class),
	VP_ENT_HDR_CTRL_OBJ		(0x46, DRW_VP_ENT_HDR_CTRL_OBJ.class),
	VP_ENT_HDR				(0x47, DRW_VP_ENT_HDR.class),
	GROUP					(0x48, DRW_GROUP.class),				//	O
	MLINESTYLE				(0x49, DRW_MLINESTYLE.class),			//	O
	OLE2FRAME				(0x4A, DRW_OLE2FRAME.class),			//	e
	LONG_TRANSACTION		(0x4C, DRW_LONG_TRANSACTION.class),		//
	LWPOLYLINE				(0x4D, DRW_LWPOLYLINE.class),			//	E
	HATCH					(0x4E, DRW_HATCH.class),				//	E
	XRECORD					(0x4F, DRW_XRECORD.class),				//	o
	ACDBPLACEHOLDER			(0x50, DRW_ACDBPLACEHOLDER.class),		//	o	aka PLACEHOLDER
	VBA_PROJECT				(0x51, DRW_VBA_PROJECT.class),			//	o
	LAYOUT					(0x52, DRW_LAYOUT.class),				//	o
	IMAGE					(0x65, DRW_IMAGE.class),				//	E
	IMAGEDEF				(0x66, DRW_IMAGEDEF.class),				//	O
	ACAD_PROXY_ENTITY		(0x1f2, DRW_ACAD_PROXY_ENTITY.class),	//	e
	ACAD_PROXY_OBJECT		(0x1f3, DRW_ACAD_PROXY_OBJECT.class),	//	o
	_LOOKUP					(0x1f4, null),	//=500,

	// non-fixed types:
	ACAD_TABLE				(0x8000, DRW_ACAD_TABLE.class),
	CELLSTYLEMAP(DRW_CELLSTYLEMAP.class),
	DBCOLOR(DRW_DBCOLOR.class),
	DICTIONARYVAR(DRW_DICTIONARYVAR.class),			//	O
	DICTIONARYWDFLT(DRW_DICTIONARYWDFLT.class),		//	O
	FIELD(DRW_FIELD.class),							//	O
	IDBUFFER(DRW_IDBUFFER.class),					//	o
	IMAGEDEFREACTOR(DRW_IMAGEDEFREACTOR.class),		//	o
	LAYER_INDEX(DRW_LAYER_INDEX.class),				//	o
	LWPLINE(DRW_LWPLINE.class),
	MATERIAL(DRW_MATERIAL.class),					//	o
	MLEADER(DRW_MLEADER.class),						//	e
	MLEADERSTYLE(DRW_MLEADERSTYLE.class),			//	o
	PLACEHOLDER(DRW_PLACEHOLDER.class),
	PLOTSETTINGS(DRW_PLOTSETTINGS.class),			//	O
	RASTERVARIABLES(DRW_RASTERVARIABLES.class),		//	o
	SCALE(DRW_SCALE.class),
	SORTENTSTABLE(DRW_SORTENTSTABLE.class),			//	o
	SPATIAL_FILTER(DRW_SPATIAL_FILTER.class),		//	o
	SPATIAL_INDEX(DRW_SPATIAL_INDEX.class),			//	o
	TABLEGEOMETRY(DRW_TABLEGEOMETRY.class),
	TABLESTYLES(DRW_TABLESTYLES.class),
	VISUALSTYLE(DRW_VISUALSTYLE.class),				//	o
	WIPEOUTVARIABLE(DRW_WIPEOUTVARIABLE.class),
	ACDBDICTIONARYWDFLT(DRW_ACDBDICTIONARYWDFLT.class),	//	o	aka DICTIONARYWDFLT
	TABLESTYLE(DRW_TABLESTYLE.class),				//	o
	EXACXREFPANELOBJECT(DRW_EXACXREFPANELOBJECT.class),
	NPOCOLLECTION(DRW_NPOCOLLECTION.class),
	ACDBSECTIONVIEWSTYLE(DRW_ACDBSECTIONVIEWSTYLE.class),
	ACDBDETAILVIEWSTYLE(DRW_ACDBDETAILVIEWSTYLE.class),
	ACDB_BLKREFOBJECTCONTEXTDATA_CLASS(DRW_ACDB_BLKREFOBJECTCONTEXTDATA_CLASS.class),
	ACDB_MTEXTATTRIBUTEOBJECTCONTEXTDATA_CLASS(DRW_ACDB_MTEXTATTRIBUTEOBJECTCONTEXTDATA_CLASS.class);

    public final int value;
	public final Class<? extends Object> clss;

    private static final class helper {
        private static int last_value = 0;
    }

    private OBJECTTYPE(Class<? extends Object> clss)			{ this.value = ++helper.last_value; this.clss = clss; }
    private OBJECTTYPE(int value, Class<? extends Object> clss)	{ this.value = helper.last_value = value; this.clss = clss; }

    public static OBJECTTYPE FromInt(int id) {
        for (OBJECTTYPE e : values()) {
            if (e.value == id)
                return e;
        }
    	return UNUSED;
    }

    public static OBJECTTYPE FromName(String name) {
        for (OBJECTTYPE e : values()) {
            if (e.name().compareTo(name) == 0)
                return e;
        }
    	return UNUSED;
    }

	static OBJECTTYPE get(bit_reader in) {
		if (in.ver(VER.R2007) < 0) {
			return FromInt(in.get16());
		} else {
			switch (in.get_bits(2)) {
				case 0:
					return FromInt(in.getc());
				case 1:
					return FromInt(in.getc() + 0x1f0);
				default:
					return FromInt(in.get16());
			}
		}
	}

	public Object newInstance(DWG dwg) {
		if (clss == null)
			return null;

		try {
			return clss.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			try {
				return clss.getDeclaredConstructor(DWG.class).newInstance(dwg);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e2) {
				return null;
			}
		}
	}

};

enum LineWidth {
	width00(0),			// 0.00mm
	width01(5),			// 0.05mm
	width02(9),			// 0.09mm
	width03(13),		// 0.13mm
	width04(15),		// 0.15mm
	width05(18),		// 0.18mm
	width06(20),		// 0.20mm
	width07(25),		// 0.25mm
	width08(30),		// 0.30mm
	width09(35),		// 0.35mm
	width10(40),		// 0.40mm
	width11(50),		// 0.50mm
	width12(53),		// 0.53mm
	width13(60),		// 0.60mm
	width14(70),		// 0.70mm
	width15(80),		// 0.80mm
	width16(90),		// 0.90mm
	width17(100),		// 1.00mm
	width18(106),		// 1.06mm
	width19(120),		// 1.20mm
	width20(140),		// 1.40mm
	width21(158),		// 1.58mm
	width22(200),		// 2.00mm
	width23(211),		// 2.11mm
	widthByLayer(-1),	// by layer
	widthByBlock(-2),	// by block
	widthDefault(-3);	// by default

 	public final int dxf;

	private LineWidth(int dxf) { this.dxf = dxf;  }

    public static LineWidth FromDXF(int i) {
		if (i < 0)
		    return	i == -1	? widthByLayer
        	    :	i == -2	? widthByBlock
				:	widthDefault;

		LineWidth	e0	= width00;
		for (LineWidth e : values()) {
			if (i <= e.dxf)
				return i * 2 < e0.dxf + e.dxf ? e0 : e;
			e0 = e;
		}
		return e0;
    }

    public static LineWidth FromDWG(int i) {
        return 	i > 0 && i <= 23 ? values()[i]
            :   i == 32 - 2 ? widthByBlock
            :   i == 32 - 1 ? widthByLayer
	        :	widthDefault;
    }
};

/*
enum ShadowMode {
	CastAndReceieveShadows = 0,
	CastShadows = 1,
	ReceiveShadows = 2,
	IgnoreShadows = 3
};

enum MaterialCodes {
	MaterialByLayer = 0
};

enum PlotStyleCodes {
	DefaultPlotStyle = 0
};

enum TransparencyCodes {
	Opaque = 0,
	Transparent = -1
};

enum VAlign {
	VBaseLine = 0,			// Top = 0
	VBottom,				// Bottom = 1
	VMiddle,				// Middle = 2
	VTop					// Top = 3
};
enum HAlign {
	HLeft = 0,				// Left = 0
	HCenter,				// Centered = 1
	HRight,					// Right = 2
	HAligned,				// Aligned = 3 (if VAlign==0)
	HMiddle,				// middle = 4 (if VAlign==0)
	HFit					// fit into point = 5 (if VAlign==0)
};
*/

static class RC implements bit_readable {
    public byte v;
    public boolean read(bit_reader in) {
        v = (byte)in.getc();
        return true;
    }
};

static class RS implements bit_readable {
    public short v;
    public boolean read(bit_reader in) {
        v = (short)in.get16();
        return true;
    }
};

static class RL implements bit_readable {
    public int v;
    public boolean read(bit_reader in) {
        v = in.get32();
        return true;
    }
};

static class RLL implements bit_readable {
    public long v;
    public boolean read(bit_reader in) {
        v = in.get32() | ((long)in.get32() << 32);
        return true;
    }
};

static class RD implements bit_readable {
    public double v;
    public boolean read(bit_reader in) {
        v = in.getDouble();
        return true;
    }
};

static class RD2 implements bit_readable {
    public double x = 0, y = 0;
	RD2() {}
	RD2(double _x, double _y) { x = _x; y = _y; }
    public boolean read(bit_reader in) {
        x = in.getDouble();
        y = in.getDouble();
        return true;
    }
};

static class RD3 implements bit_readable {
    public double x = 0, y = 0, z = 0;
	RD3() {}
	RD3(double _x, double _y, double _z) { x = _x; y = _y; z = _z;}
    public boolean read(bit_reader in) {
        x = in.getDouble();
        y = in.getDouble();
        z = in.getDouble();
        return true;
    }
};

// bit (1 or 0)
static class B implements bit_readable {
	boolean	v = false;
    public boolean read(bit_reader in) {
        v = in.get_bit();
         return true;
    }
};

// bitshort (16 bits)
static class BS implements bit_readable {
	short	v = 0;
    public boolean read(bit_reader in) {
		switch (in.get_bits(2)) {
			case 0: v = (short)in.get16(); break;
			case 1: v = (short)in.getc(); break;
			case 2: v = 0; break;
			case 3: v = 256; break;
		}
		return true;
	}
	public static BS get(bit_reader in) {
		var	t = new BS();
		t.read(in);
		return t;
	}
};

// BS R2000+, byte on R13,R14
static class BSV extends BS {
    public boolean read(bit_reader in) {
		if (in.ver(VER.R2000) < 0) {
			v = (short)in.getc();
			return true;
		}
		return super.read(in);
	}
};

// bitlong (32 bits)
static class BL implements bit_readable {
	int	v = 0;
    public boolean read(bit_reader in) {
		switch (in.get_bits(2)) {
			case 0: v = in.get32(); return true;
			case 1: v = in.getc(); return true;
			default:
			case 2: v = 0; return true;
			//default: return false;
		}
	}
	public static BL get(bit_reader in) {
		var t = new BL();
		t.read(in);
		return t;
	}
};

// bitlonglong (64 bits) (R24)
static class BLL implements bit_readable {
	long	v = 0;
    public boolean read(bit_reader in) {
		int	n = in.get_bits(3);
		for (int i = 0; i < n; i++)
			v = (v << 8) | in.getc();
		return true;
	}
};

// bitdouble
static class BD extends RD {
	double	v = 0;
	BD() {}
	BD(double _v) { v = _v; }
    public boolean read(bit_reader in) {
		switch (in.get_bits(2)) {
			case 0: return super.read(in);
			case 1: v = 1; return true;
			default:
			case 2: v = 0; return true;
			//default: return false;
		}
	}
	
	static BD get(bit_reader in) {
		var t = new BD();
		t.read(in);
		return t;
	}
};

static class BD2 implements bit_readable {
    BD  x, y;
	BD2() { instantiator.instantiate_fields(this, bit_readable.class); }
    public boolean read(bit_reader in) {
        return x.read(in) && y.read(in);
    }
};

static class BD3 implements bit_readable {
    BD x, y, z;
	BD3() { bit_readable.instantiate(this); }
    public boolean read(bit_reader in) {
        return x.read(in) && y.read(in) && z.read(in);
    }
	static BD3 get(bit_reader in) {
		var t = new BD3();
		t.read(in);
		return t;
	}

};

// modular char
static class MC implements bit_readable {
	long	v = 0;
	public boolean	read(reader in) {
		long	r = 0;
		for (int i = 0; i < 64; i += 7) {
			int c = in.getc();
			r |= (long)(c & 0x7f) << i;
			if ((c & 0x80) == 0)
				break;
		}
		v = r;
		return true;
	}
	public boolean	read(bit_reader in) {
		return read((reader)in);
	}
	static MC get(reader in) {
		var		t = new MC();
		t.read(in);
		return t;
	}
};

// modular char (signed)
static class MCS implements bit_readable {
	long	v = 0;
	MCS()			{ v = 0; }
	MCS(long _v) 	{ v = _v;}
	public boolean	read(reader in) {
		long	r = 0;
		for (int i = 0; i < 64; i += 7) {
			int c = in.getc();
			r |= (long)(c & 0x7f) << i;
			if ((c & 0x80) == 0) {
				if ((c & 0x40) != 0)
					r = ((long)0x40 << i) - r;
				break;
			}
		}
		v = r;
		return true;
	}
	public boolean read(bit_reader in) {
		return read((reader) in);
	}
	static MCS get(reader in) {
		var t = new MCS();
		t.read(in);
		return t;
	}
};

// Variable text, T for 2004 and earlier files, TU for 2007+ files.
static class TV implements bit_readable {
    String  s;
	public boolean	read(bit_reader in) {
		var	sbits 	= ((bitsin2)in).sbits;
		int	len 	= BS.get(sbits).v;
		if (in.ver(VER.R2007) >= 0) {
			var	data = new char[len];
			sbits.read(data);
			s	= new String(data);
		} else {
			s	= new String(sbits.readbuff(len));
		}
		return true;
	}
	static TV get(bit_reader in) {
		var t = new TV();
		t.read(in);
		return t;
	}
};

// BitExtrusion
static class BEXT extends BD3 {
	public boolean	read(bit_reader in)	{
		if (in.ver(VER.R2000) >= 0 && in.get_bit())
			return true;
		return super.read(in);
	}
};

	// BitDouble With Default
static class DD {
	static double adjust(bit_reader in, double v) {
		byte[] bytes = new byte[8];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.putDouble(v);

		switch (in.get_bits(2)) {
			case 0:
				break;
			case 1: {
				in.readbuff(bytes, 0, 4);
				break;
			}
			case 2: {
				in.readbuff(bytes, 4, 2);
				in.readbuff(bytes, 0, 4);
				break;
			}
			case 3:
				in.readbuff(bytes, 0, 8);
				break;
		}
		return bb.getDouble(0);

	}

	static double[] adjust(bit_reader in, double[] def, int n) {
		double[] out = new double[n];
		for (int i = 0; i < n; i++)
			out[i] = adjust(in, def[i]);
		return out;
	}
};

// BitScale
static class BSCALE extends BD3 {
	BSCALE() {
        x.v = y.v = z.v = 1.0;
    }

	public boolean	read(bit_reader bits)	{
		if (bits.ver(VER.R14) <= 0)
			return super.read(bits);

		switch (bits.get_bits(2)) {
			case 0:
				x.v = bits.getDouble();
				// fallthrough
			case 1: //x default value 1, y & z can be x value
				y.v = DD.adjust(bits, x.v);
				z.v = DD.adjust(bits, x.v);
				break;
			case 2:
				x.v = y.v = z.v = bits.getDouble();
				// fallthrough
			case 3:
				break;
		}
		return true;
	}
};


// BitThickness
static class BT extends BD {
	public boolean read(bit_reader in)	{
		if (in.ver(VER.R2000) >= 0 && in.get_bit())
			return true;
		return super.read(in);
	}
};

// Handle

static class H implements bit_readable {
	static final int SoftOwnerRef	= 2;
	static final int HardOwnerRef	= 3;
	static final int SoftPointerRef	= 4;
	static final int HardPointerRef	= 5;
	static final int AddOne			= 6;
	static final int SubOne			= 8;
	static final int AddOffset		= 10;
	static final int SubOffset		= 12;

    int encoded = 0;

    final int code()    { return encoded >> 28; }
    final int offset()  { return encoded & 0xffffff; }

	public boolean read(bitsin in) {
		int c = in.getc();
		int	val	= 0;
		for (int i = 0, n = c & 15; i < n; i++)
			val = (val << 8) | in.getc();
		encoded = val + (c << 24);
		return true;
	}
	
	public boolean	read(bit_reader in)	{
		if (in instanceof bitsin3)
			return read(((bitsin3)in).hbits);
		return read((bitsin)in);
	}
	
	static H get(bit_reader in) {
		var t = new H();
		t.read(in);
		return t;
	}

	final int	get_offset(int href) {
		switch (code()) {
			case AddOne:	return href + 1;
			case SubOne:	return href - 1;
			case AddOffset: return href + offset();
			case SubOffset: return href - offset();
			default:		return offset();
		}
	}
};

static class HandleRange implements bit_readable {
	H[]	handles;	//pre 2004, firstEH, lastEH, seqendH; else array + seqendH

	//dummy
	public boolean read(bit_reader bits) {
		return false;
	}

	boolean	read(bit_reader bits, int count) {
		if (count < 0)
            return true;
        handles = new H[bits.ver(VER.R2004) >= 0 ? count + 1 : 3];
		return bits.read(handles);
	}
	
	final H	endH() {
		return handles[handles.length - 1];
	}
};

class HandleCollection implements Iterable<Entity> {
	HandleRange range;
	int			h;

	class It0 implements Iterator<Entity> {
		int		i, e;

		It0() {
			i = range.handles[0].get_offset(h);
			e = range.handles[1].get_offset(h);
		}
 
		public boolean	hasNext() { return i != e; }
		public Entity	next()	{
			var	ent = (Entity) get_object(i);
			i = ent != null ? ent.next_ent : e;
			return ent;
		}
	};
	class It1 implements Iterator<Entity> {
		int		i = 0;
		public boolean	hasNext() 	{ return i < range.handles.length; }
		public Entity	next()		{ return (Entity)get_object(range.handles[i++].offset()); }
	};
	
	public Iterator<Entity>	iterator() {
		return version.compareTo(VER.R2000) <= 0 ? new It0() : new It1();
	}

	HandleCollection(HandleRange _range, int _h) {
		range = _range;
		h = _h;
	}
}

static class CMC implements bit_readable {
    static final int ByLayer    = 0xC0;
    static final int ByBlock	= 0xC1;
    static final int RGB		= 0xC2;
    static final int ACIS	    = 0xC3;

	BS		index;
	BL		rgb;
	byte	name_type = 0;
	TV		name;

	public boolean read(bit_reader in) {
		bit_readable.instantiate(this);
		index.read(in);
		if (in.ver(VER.R2000) >= 0) {
			rgb = new BL();
			in.read(rgb);
			if ((name_type = (byte)in.getc()) != 0) {
				name = new TV();
				name.read(in);
			}
		}
		return true;
	}
};

static class ENC implements bit_readable {
	static final int Complex		= 0x8000;
	static final int AcDbRef		= 0x4000;
	static final int Transparency	= 0x2000;
	BS	flags;
	BL	rgb;
	BL	transparency;
	H	h;

	public boolean read(bit_reader in)	{
		bit_readable.instantiate(this);
		in.read(flags);
		if ((flags.v & Complex) != 0) {
			rgb.read(in);
			if ((flags.v & AcDbRef) != 0)
				h.read(in);
		}
		if ((flags.v & Transparency) != 0)
			transparency.read(in);
		return true;
	}

};

static class TIME implements bit_readable {
	BL	day = new BL(), msec = new BL();
	public boolean	read(bit_reader in)	{
		return day.read(in) && msec.read(in);
	}
};

static class RenderMode implements bit_readable {
	RC	    mode;
	B		use_default_lights;
	RC	    default_lighting_type;
	BD		brightness;
	BD		contrast;
	CMC		ambient;

    public boolean read(bit_reader bits) {
		bit_readable.instantiate(this);
		return bits.read(mode) && (bits.ver(VER.R2004) <= 0 || (bits.read(use_default_lights, default_lighting_type, brightness, contrast) && ambient.read(bits)));
	}
};

static class UserCoords implements bit_readable {
	BD3		origin, xdir, ydir;
	BD		elevation;
	BS		ortho_view_type;

    public boolean read(bit_reader bits) {
		bit_readable.instantiate(this);
		return bits.read(origin, xdir, ydir) && (bits.ver(VER.R2000) < 0 || bits.read(elevation, ortho_view_type));
	}
};

static class Gradient implements bit_readable {
	static class Entry implements bit_readable {
		BD		unkDouble;
		BS		unkShort;
		BL		rgbCol;
		RC  	ignCol;

        public boolean read(bit_reader bits) {
			bit_readable.instantiate(this);
			return bits.read(unkDouble, unkShort, rgbCol, ignCol);
		}
	};
	BL		isGradient;
	BL		res;
	BD		gradAngle;
	BD		gradShift;
	BL		singleCol;
	BD		gradTint;
	Entry[]	entries;

    public boolean	read(bit_reader bits)	{
		bit_readable.instantiate(this);
		if (!bits.read(isGradient, res, gradAngle, gradShift, singleCol, gradTint))
            return false;
        entries = new Entry[BL.get(bits).v];
		return bits.read(entries);
	}
};
/*

enum CONTENT_PROPS {
	DataType				= 1 << 0,
	DataFormat				= 1 << 1,
	Rotation				= 1 << 2,
	BlockScale				= 1 << 3,
	Alignment				= 1 << 4,
	ContentColor			= 1 << 5,
	TextStyle				= 1 << 6,
	TextHeight				= 1 << 7,
	AutoScale				= 1 << 8,
	//Cell style properties:
	BackgroundColor			= 1 << 9,
	MarginLeft				= 1 << 10,
	MarginTop				= 1 << 11,
	MarginRight				= 1 << 12,
	MarginBottom			= 1 << 13,
	ContentLayout			= 1 << 14,
	MarginHorizontalSpacing	= 1 << 17,
	MarginVerticalSpacing	= 1 << 18,
	//Row/column properties:
	MergeAll				= 1 << 15,
	//Table properties:
	FlowBottomToTop			= 1 << 16,
};
*/

static class ValueSpec implements bit_readable {
	static final int Unknown	= 0;	//Unknown BL
	static final int Long		= 1;	//Long BL
	static final int Double		= 2;	//Double BD
	static final int String		= 4;	//String TV
	static final int Date		= 8;	//Date BL data size N, followed by N bytes (Int64 value)
	static final int Point2D	= 16;	//Point BL data size, followed by 2RD 
	static final int Point3D	= 32;	//3D Point BL data size, followed by 3RD
	static final int Object		= 64;	//Object Id H Read from appropriate place in handles section (soft pointer).
	static final int BufferUnk	= 128;	//Buffer Unknown.
	static final int BufferRes	= 256;	//Result Buffer Unknown.
	static final int General	= 512;	//General General, BL containing the byte count followed by a byte array. (introduced in R2007, use Unknown before R2007).

	//enum UNIT_TYPE {
	static final int no_units	= 0;
	static final int distance	= 1;
	static final int angl		= 2;
	static final int area		= 4;
	static final int volume		= 7;

	BL		flags;
	BL		data_type;
	BL		unit_type;
	TV		format;

	public boolean read(bit_reader bits) {
		if (bits.ver(VER.R2007) < 0) {
			bits.read(data_type);
		} else {
			bits.read(flags);//Flags BL 93 Flags & 0x01 => type is kGeneral
			if ((flags.v & 1) == 0)
				bits.read(data_type);
			bits.read(unit_type, format);
		}
		return true;
	}
};

static class Value extends ValueSpec {
	TV		value;
	boolean read(bitsin2 bits) {
		return super.read(bits) && (bits.ver(VER.R2007) < 0 || bits.read(value));
	}
	public static Value get(bitsin2 bits) {
		var	t = new Value();
		t.read(bits);
		return t;
	}
};

static class ContentFormat {
	//enum ALIGN {
	//	TopLeft			= 1,
	//	TopCenter		= 2,
	//	TopRight		= 3,
	//	MiddleLeft		= 4,
	//	MiddleCenter	= 5,
	//	MiddleRight		= 6,
	//	BottomLeft		= 7,
	//	BottomCenter	= 8,
	//	BottomRight		= 9
	//};
	BL		PropertyOverrideFlags;
	BL		PropertyFlags;// Contains property bit values for property Auto Scale only (0x100).
	BL		data_type;
	BL		unit_type;
	TV		format;

	BD		rotation;
	BD		scale;
	BL		alignment;
	CMC		color;
	H		TextStyle;
	BD		TextHeight;

	boolean read(bit_reader bits) {
		return bits.read(PropertyOverrideFlags, PropertyFlags, data_type, unit_type, format, rotation, scale, alignment, color, TextHeight);
	}
};

static class RowStyle implements bit_readable {
	class Border {
		BS		line_weight;
		B		visible;
		CMC		colour;
		boolean	read(bitsin2 bits) { return bits.read(line_weight, visible, colour); }
	};
	H		text_style;
	BD		text_height;
	BS		text_align;
	CMC		text_colour, fill_colour;
	B		bk_color_enabled;
	Border	top, horizontal, bottom, left, vertical, right;
	//2007+
	BL		data_type, data_unit_type;
	TV		format;

	public boolean	read(bit_reader bits) {
		return bits.read(text_style, text_height, text_align, text_colour, fill_colour, bk_color_enabled)
			&& (bits.ver(VER.R2007) < 0 || bits.read(data_type, data_unit_type, format));
	}
};


static class CellStyle implements bit_readable {
	//enum ID {
	//	title	= 1,
	//	header	= 2,
	//	data	= 3,
	//	table	= 4,
	//	Custom	= 101,//cell style ID’s are numbered starting at 101
	//};
	//enum CLASS {
	//	ClassData	= 1,
	//	ClassLabel	= 2,
	//};
	//enum STYLE {
	//	Cell = 1,
	//	Row = 2,
	//	Column = 3,
	//	Formatted = 4,
	//	Table = 5
	//};
	//enum LAYOUT_FLAGS {
	//	Flow = 1,
	//	StackedHorizontal = 2,
	//	StackedVertical = 4
	//};
	//enum EDGE_FLAGS {
	//	top			= 1 << 0,
	//	right		= 1 << 1,
	//	bottom		= 1 << 2,
	//	left		= 1 << 3,
	//	vertical	= 1 << 4,
	//	horizontal	= 1 << 5,
	//};
	//enum BORDER_FLAGS {
	//	BorderTypes = 0x1,
	//	LineWeight = 0x2,
	//	LineType = 0x4,
	//	Color = 0x8,
	//	Invisibility = 0x10,
	//	DoubleLineSpacing = 0x20
	//};
	//enum BORDER_TYPE {
	//	Single = 1,
	//	Double = 2
	//};

	class Border implements bit_readable {
		BL	EdgeFlags;
		BL	BorderPropertyOverride;
		BL	BorderType;
		CMC	Color;
		BL	LineWeight;
		H	LineLtype;
		BL	Invisibility;//: 1 = invisible, 0 = visible.
		BD	DoubleLineSpacing;

		//boolean	read(bitsin3 bits) {

		public boolean read(bit_reader bits) {
			bits.read(EdgeFlags);
			return EdgeFlags.v == 0 || bits.read(BorderPropertyOverride, BorderType, Color, LineWeight, LineLtype, Invisibility, DoubleLineSpacing);
		}
	};

	BL		style_type;
	BL		PropertyOverrideFlags;
	BL		MergeFlags;				// only for bits 0x8000 and 0x10000
	CMC		BackgroundColor;
	BL		ContentLayoutFlags;
	ContentFormat	ContentFormat;
	BD		VerticalMargin, HorizontalMargin, BottomMargin, RightMargin, MarginHorizontalSpacing, MarginVerticalSpacing;
	Border[]	borders;

	BL	id;
	BL	type;
	TV	name;

	public boolean	read(bit_reader bits) {
		bits.read(style_type);
		if (BS.get(bits).v != 0) {
			bits.read(PropertyOverrideFlags, MergeFlags, BackgroundColor, ContentLayoutFlags);
			ContentFormat.read(bits);
			if ((BS.get(bits).v & 1) != 0)
				bits.read(VerticalMargin, HorizontalMargin, BottomMargin, RightMargin, MarginHorizontalSpacing, MarginVerticalSpacing);
			bits.read(borders = new Border[BL.get(bits).v]);
		}
		return bits.read(id, type, name);
	}
};

//-----------------------------------------------------------------------------
// DimStyle
//-----------------------------------------------------------------------------

static class DimStyle implements bit_readable {
	static final int DIMTOL			= 1 << 0;
	static final int DIMLIM			= 1 << 1;
	static final int DIMTIH			= 1 << 2;
	static final int DIMTOH			= 1 << 3;
	static final int DIMSE1			= 1 << 4;
	static final int DIMSE2			= 1 << 5;
	static final int DIMALT			= 1 << 6;
	static final int DIMTOFL		= 1 << 7;
	static final int DIMSAH			= 1 << 8;
	static final int DIMTIX			= 1 << 9;
	static final int DIMSOXD		= 1 << 10;
	static final int DIMSD1			= 1 << 11;
	static final int DIMSD2			= 1 << 12;
	static final int DIMUPT			= 1 << 13;
	static final int DIMFXLON		= 1 << 14;
	static final int DIMTXTDIRECTION= 1 << 15;

    int		flags;

	TV		DIMPOST, DIMAPOST;
	TV		DIMBLK, DIMBLK1, DIMBLK2, DIMALTMZS, DIMMZS;
	BSV		DIMALTD, DIMZIN, DIMTOLJ, DIMJUST,DIMFIT, DIMTZIN, DIMALTZ, DIMALTTZ, DIMTAD;

	RC	    DIMUNIT;	//r13/14 only

	BS		DIMAUNIT, DIMDEC, DIMTDEC, DIMALTU, DIMALTTD;
	BD		DIMSCALE, DIMASZ, DIMEXO, DIMDLI, DIMEXE, DIMRND, DIMDLE, DIMTP, DIMTM, DIMTXT, DIMCEN, DIMTSZ, DIMALTF, DIMLFAC, DIMTVP, DIMTFAC,DIMGAP;
	CMC		DIMCLRD, DIMCLRE, DIMCLRT;

	//>13,14
	BD		DIMFXL, DIMJOGANG;
	BS		DIMTFILL;
	CMC		DIMTFILLCLR;
	BS		DIMAZIN, DIMARCSYM;
	BD		DIMALTRND;
	BS		DIMADEC, DIMFRAC, DIMLUNIT, DIMDSEP, DIMTMOVE;
	BD		DIMALTMZF;
	BS		DIMLWD, DIMLWE;
	BD		DIMMZF;
	H		DIMTXSTY, DIMLDRBLK;
	H		HDIMBLK, HDIMBLK1, HDIMBLK2, DIMLTYPE, DIMLTEX1, DIMLTEX2;

	//boolean parse(bitsin3 &bits) {
    public boolean read(bit_reader bits) {
		bit_readable.instantiate(this);
		//	R13 & R14 Only:
		if (bits.ver(VER.R14) <= 0) {
			flags = bits.getc() | (bits.get_bits(3) << 8);
			bits.read(DIMALTD, DIMZIN);
			flags |= bits.get_bits(2) * DIMSD1;
			bits.read(DIMTOLJ, DIMJUST, DIMFIT);
			flags |= bits.get_bits(1) * DIMUPT;
			bits.read(DIMTZIN, DIMALTZ,DIMALTTZ, DIMTAD, DIMUNIT, DIMAUNIT, DIMDEC, DIMTDEC, DIMALTU, DIMALTTD);

		}
		bits.read(DIMPOST, DIMAPOST, DIMSCALE, DIMASZ, DIMEXO, DIMDLI, DIMEXE, DIMRND, DIMDLE, DIMTP, DIMTM);

		if (bits.ver(VER.R2007) >= 0)
			bits.read(DIMFXL, DIMJOGANG, DIMTFILL, DIMTFILLCLR);

		if (bits.ver(VER.R2000) >= 0) {
			flags = bits.get_bits(6);
			bits.read(DIMTAD, DIMZIN, DIMAZIN);
		}
		if (bits.ver(VER.R2007) >= 0)
			bits.read(DIMARCSYM);

		bits.read(DIMTXT, DIMCEN, DIMTSZ, DIMALTF, DIMLFAC, DIMTVP, DIMTFAC, DIMGAP);

		if (bits.ver(VER.R14) <= 0) {
			bits.read(DIMPOST, DIMAPOST, DIMBLK, DIMBLK1, DIMBLK2);
		} else {
			bits.read(DIMALTRND);
			flags |= bits.with_flag(DIMALT);
			bits.read(DIMALTD);
			flags |= bits.get_bits(4) * DIMTOFL;
		}
		
		bits.read(DIMCLRD, DIMCLRE, DIMCLRT);

		if (bits.ver(VER.R2000) >= 0) {
			bits.read(DIMADEC, DIMDEC, DIMTDEC, DIMALTU, DIMALTTD, DIMAUNIT, DIMFRAC, DIMLUNIT, DIMDSEP, DIMTMOVE, DIMJUST);
			flags |= bits.get_bits(2) * DIMSD1;
			bits.read(DIMTOLJ, DIMTZIN, DIMALTZ, DIMALTTZ);
			flags |= bits.with_flag(DIMUPT);
			bits.read(DIMFIT);

			if (bits.ver(VER.R2007) >= 0)
				flags |= bits.with_flag(DIMFXLON);

			if (bits.ver(VER.R2010) >= 0) {
				flags |= bits.with_flag(DIMTXTDIRECTION);
				bits.read(DIMALTMZF, DIMALTMZS, DIMMZS, DIMMZF);
			}

			//handles
			bits.read(DIMTXSTY, DIMLDRBLK, HDIMBLK, HDIMBLK1, HDIMBLK2);
			if (bits.ver(VER.R2007) >= 0)
				bits.read(DIMLTYPE, DIMLTEX1, DIMLTEX2);
			bits.read(DIMLWD, DIMLWE);
		}
		return true;
	}
};

	// -----------------------------------------------------------------------------
	// decompression
	// -----------------------------------------------------------------------------

	static class decompress_dwg {
		final byte[] compBuffer;
		int 	compPos 	= 0;
		boolean compGood 	= true;

		byte[] 	decompBuffer;
		int 	decompPos 	= 0;
		boolean decompGood 	= true;

		boolean buffersGood() {
			return compGood && decompGood;
		}

		int compressedByte() {
			return (compGood = compPos < compBuffer.length) ? compBuffer[compPos++] & 0xff : 0;
		}

		void decompSet(byte value) {
			if (decompGood = decompPos < decompBuffer.length)
				decompBuffer[decompPos++] = value;
		}

		boolean copy(int offset, int count) {
			if (offset > decompPos || decompPos + count >= decompBuffer.length)
				return false;

			for (int end = decompPos + count; decompPos < end; ++decompPos)
				decompBuffer[decompPos] = decompBuffer[decompPos - offset];
			return true;
		}

		decompress_dwg(final byte[] _compressedBuffer, byte[] _decompBuffer) {
			compBuffer = _compressedBuffer;
			decompBuffer = _decompBuffer;
		}
	};

	// -----------------------------------------------------------------------------
	// HeaderVars
	// -----------------------------------------------------------------------------

	static class UCSstuff implements bit_readable {
		BD3		INSBASE, EXTMIN, EXTMAX;
		RD2		LIMMIN, LIMMAX;
		BD		ELEVATION;
		BD3		ORG, XDIR, YDIR;
		H		NAME;
		//R2000+
		H		ORTHOREF;
		BS		ORTHOVIEW;
		H		BASE;
		BD3		ORGTOP, ORGBOTTOM, ORGLEFT, ORGRIGHT, ORGFRONT, ORGBACK;

		public boolean read(bit_reader bits) {
			bit_readable.instantiate(this);
			return bits.read(INSBASE, EXTMIN, EXTMAX, LIMMIN, LIMMAX, ELEVATION, ORG, XDIR, YDIR, NAME)
				&& (bits.ver(VER.R2000) < 0 || bits.read(ORTHOREF,ORTHOVIEW,BASE,ORGTOP, ORGBOTTOM, ORGLEFT, ORGRIGHT, ORGFRONT, ORGBACK));
		}
	};

class HeaderVars {
	BLL			requiredVersions;

	B			DIMASO, DIMSHO;
	B			DIMSAV;
	B			PLINEGEN, ORTHOMODE, REGENMODE, FILLMODE, QTEXTMODE, PSLTSCALE, LIMCHECK, BLIPMODE, USRTIMER, SKPOLY, ANGDIR, SPLFRAME;
	B			ATTREQ, ATTDIA;
	B			MIRRTEXT, WORLDVIEW;
	B			WIREFRAME;
	B			TILEMODE, PLIMCHECK, VISRETAIN;
	B			DELOBJ;
	B			DISPSILH, PELLIPSE;
	BS			PROXIGRAPHICS;
	BS			DRAGMODE;//RLZ short or bit??
	BS			TREEDEPTH, LUNITS, LUPREC, AUNITS, AUPREC;
	BS			OSMODE;
	BS			ATTMODE;
	BS			COORDS;
	BS			PDMODE;
	BS			PICKSTYLE;

	BS			USERI1, USERI2, USERI3, USERI4, USERI5, SPLINESEGS, SURFU, SURFV, SURFTYPE, SURFTAB1, SURFTAB2, SPLINETYPE, SHADEDGE, SHADEDIF, UNITMODE, MAXACTVP, ISOLINES, CMLJUST, TEXTQLTY;
	BD			LTSCALE, TEXTSIZE, TRACEWID, SKETCHINC, FILLETRAD, THICKNESS, ANGBASE, PDSIZE, PLINEWID, USERR1, USERR2, USERR3, USERR4, USERR5, CHAMFERA, CHAMFERB, CHAMFERC, CHAMFERD, FACETRES, CMLSCALE, CELTSCALE;
	TV			MENU;

	TIME		TDCREATE, TDUPDATE, TDINDWG, TDUSRTIMER;
	CMC			CECOLOR;

	H			HANDSEED;//always present in data stream
	H			CLAYER, TEXTSTYLE, CELTYPE;
	H			CMATERIAL;
	H			DIMSTYLE,  CMLSTYLE;

	BD 			PSVPSCALE;
	
	UCSstuff	PUCS;
	UCSstuff	UCS;
	DimStyle	dim;

	H			BLOCK_CONTROL, LAYER_CONTROL, TEXTSTYLE_CONTROL, LINETYPE_CONTROL, VIEW_CONTROL, UCS_CONTROL, VPORT_CONTROL, APPID_CONTROL, DIMSTYLE_CONTROL;
	H			VP_ENT_HDR_CONTROL;
	H			GROUP_CONTROL, MLINESTYLE_CONTROL;

	//R2000+
	H			DICT_NAMED_OBJS;
	BS			TSTACKALIGN, TSTACKSIZE;
	TV			HYPERLINKBASE, STYLESHEET;
	H			LAYOUTS_CONTROL, PLOTSETTINGS_CONTROL, DICT_PLOTSTYLES;
	H			DICT_MATERIALS, DICT_COLORS;
	H			DICT_VISUALSTYLE;
	H			DICT_UNKNOWN;
	BS			INSUNITS, CEPSNTYPE;
	H			CPSNID;
	TV			FINGERPRINTGUID, VERSIONGUID;

	//R2004+
	RC			SORTENTS, INDEXCTL, HIDETEXT, XCLIPFRAME, DIMASSOC, HALOGAP;
	BS			OBSCUREDCOLOR, INTERSECTIONCOLOR;
	RC			OBSCUREDLTYPE, INTERSECTIONDISPLAY;
	TV			PROJECTNAME;

	//common
	H			BLOCK_PAPER_SPACE, BLOCK_MODEL_SPACE, LTYPE_BYLAYER, LTYPE_BYBLOCK, LTYPE_CONTINUOUS;

	//R2007+
	B			CAMERADISPLAY;
	BD			STEPSPERSEC, STEPSIZE, _3DDWFPREC, LENSLENGTH, CAMERAHEIGHT;
	RC			SOLIDHIST, SHOWHIST;
	BD			PSOLWIDTH, PSOLHEIGHT, LOFTANG1, LOFTANG2, LOFTMAG1, LOFTMAG2;
	BS			LOFTPARAM;
	RC			LOFTNORMALS;
	BD			LATITUDE, LONGITUDE, NORTHDIRECTION;
	BL			TIMEZONE;
	RC			LIGHTGLYPHDISPLAY, TILEMODELIGHTSYNCH, DWFFRAME, DGNFRAME;
	CMC			INTERFERECOLOR;
	H			INTERFEREOBJVS, INTERFEREVPVS, DRAGVS;
	RC			CSHADOW;

	HeaderVars(bitsin3 bits) {
		bit_readable.instantiate(this);

    	if (bits.ver(VER.R2013) >= 0)
            bits.read(requiredVersions);

        //unknown
        bits.read(new BD(), new BD(), new BD(), new BD(), new TV(), new TV(), new TV(), new TV(), new BL(), new BL());

    	if (bits.ver(VER.R14) <= 0)
            bits.read(new BS());

        if (bits.ver(VER.R2000) <= 0)
            bits.read(new H());//hcv    (main bits)

        bits.read(DIMASO, DIMSHO);
    	if (bits.ver(VER.R14) <= 0)
            bits.read(DIMSAV);

        bits.read(PLINEGEN, ORTHOMODE, REGENMODE, FILLMODE, QTEXTMODE, PSLTSCALE, LIMCHECK, BLIPMODE, USRTIMER, SKPOLY, ANGDIR, SPLFRAME);
    	if (bits.ver(VER.R14) <= 0)
            bits.read(ATTREQ, ATTDIA);
        bits.read(MIRRTEXT, WORLDVIEW);
    	if (bits.ver(VER.R14) <= 0)
            bits.read(WIREFRAME);
        bits.read(TILEMODE, PLIMCHECK, VISRETAIN);
    	if (bits.ver(VER.R14) <= 0)
	        bits.read(DELOBJ);
        bits.read(DISPSILH, PELLIPSE, PROXIGRAPHICS);
    	if (bits.ver(VER.R14) <= 0)
            bits.read(DRAGMODE);
        bits.read(TREEDEPTH, LUNITS, LUPREC, AUNITS, AUPREC);
    	if (bits.ver(VER.R14) <= 0)
            bits.read(OSMODE);
        bits.read(ATTMODE);
    	if (bits.ver(VER.R14) <= 0)
            bits.read(COORDS);
        bits.read(PDMODE);
    	if (bits.ver(VER.R14) <= 0)
            bits.read(PICKSTYLE);

    	if (bits.ver(VER.R2004) >= 0)
            bits.read(new BL(), new BL(), new BL());	//unknown

        bits.read(
            USERI1, USERI2, USERI3, USERI4, USERI5, SPLINESEGS,
            SURFU, SURFV, SURFTYPE, SURFTAB1, SURFTAB2, SPLINETYPE,
            SHADEDGE, SHADEDIF, UNITMODE, MAXACTVP,ISOLINES, CMLJUST, TEXTQLTY, LTSCALE, TEXTSIZE, TRACEWID, SKETCHINC, FILLETRAD, THICKNESS, ANGBASE, PDSIZE, PLINEWID,
            USERR1, USERR2, USERR3, USERR4, USERR5,
            CHAMFERA, CHAMFERB, CHAMFERC, CHAMFERD,
            FACETRES, CMLSCALE,CELTSCALE,
            MENU,
            TDCREATE, TDUPDATE
        );

    	if (bits.ver(VER.R2004) >= 0)
            bits.read(new BL(), new BL(), new BL());	//unknown

        bits.read(TDINDWG, TDUSRTIMER, CECOLOR);
		HANDSEED.read((bitsin)bits);

		bits.read(CLAYER, TEXTSTYLE, CELTYPE);
    	if (bits.ver(VER.R2007) >= 0)
            bits.read(CMATERIAL);
        bits.read(DIMSTYLE, CMLSTYLE);
    	if (bits.ver(VER.R2000) >= 0)
            bits.read(PSVPSCALE);

        PUCS.read(bits);
        UCS.read(bits);
        dim.read(bits);

        bits.read(BLOCK_CONTROL, LAYER_CONTROL, TEXTSTYLE_CONTROL, LINETYPE_CONTROL, VIEW_CONTROL, UCS_CONTROL, VPORT_CONTROL, APPID_CONTROL, DIMSTYLE_CONTROL);
        
    	if (bits.ver(VER.R2000) <= 0)
			bits.read(VP_ENT_HDR_CONTROL);
        bits.read(GROUP_CONTROL, MLINESTYLE_CONTROL);

        if (bits.ver(VER.R2000) >= 0) {
            bits.read(DICT_NAMED_OBJS, TSTACKALIGN, TSTACKSIZE, HYPERLINKBASE, STYLESHEET, LAYOUTS_CONTROL, PLOTSETTINGS_CONTROL, DICT_PLOTSTYLES);
    	    if (bits.ver(VER.R2004) >= 0)
                bits.read(DICT_MATERIALS, DICT_COLORS);
        	if (bits.ver(VER.R2007) >= 0)
                bits.read(DICT_VISUALSTYLE);
        	if (bits.ver(VER.R2013) >= 0)
                bits.read(DICT_UNKNOWN);
            bits.read(new BL());//	flags(bits);
            bits.read(INSUNITS, CEPSNTYPE);
            if (CEPSNTYPE.v == 3)
                bits.read(CPSNID);

            bits.read(FINGERPRINTGUID, VERSIONGUID);
        	if (bits.ver(VER.R2004) >= 0)
                bits.read(SORTENTS, INDEXCTL, HIDETEXT, XCLIPFRAME, DIMASSOC, HALOGAP, OBSCUREDCOLOR, INTERSECTIONCOLOR, OBSCUREDLTYPE, INTERSECTIONDISPLAY, PROJECTNAME);
        }
        bits.read(BLOCK_PAPER_SPACE, BLOCK_MODEL_SPACE, LTYPE_BYLAYER, LTYPE_BYBLOCK, LTYPE_CONTINUOUS);

    	if (bits.ver(VER.R2007) >= 0) {
            bits.read(CAMERADISPLAY);
            bits.read(new BL(), new BL(), new BD());
            bits.read(
                STEPSPERSEC, STEPSIZE, _3DDWFPREC, LENSLENGTH, CAMERAHEIGHT, SOLIDHIST, SHOWHIST, PSOLWIDTH, PSOLHEIGHT,
                LOFTANG1, LOFTANG2, LOFTMAG1, LOFTMAG2, LOFTPARAM, LOFTNORMALS,
                LATITUDE, LONGITUDE, NORTHDIRECTION, TIMEZONE, LIGHTGLYPHDISPLAY, TILEMODELIGHTSYNCH, DWFFRAME, DGNFRAME
            );
            bits.read(new B());
            bits.read(INTERFERECOLOR, INTERFEREOBJVS, INTERFEREVPVS, DRAGVS, CSHADOW);
            bits.read(new BD());
        }
        if (bits.ver(VER.R14) >= 0)
            bits.read(new BS(), new BS(), new BS(), new BS());
    }
};
/*

typedef variant<string, boolean, int32, int, double, RD2, RD3, TIME, CMC, H, malloc_block> _variant;
static class Variant : _variant {
	template<typename T> Variant(const T &t) : _variant(global_get(t)) {}
};

Variant read_extended1(bitsin bits, int remaining) {
	auto	dxfCode = bits.getc();
	switch (dxfCode + 1000) {
		case DXF_STRING:
			if (bits.ver(VER.R2004) <= 0) {
				int	len = bits.getc();
				short	cp	= RS.get(bits);
				return string(bits, len);
			}
			return (string)string16(bits, RS.get(bits));

		case DXF_BRACKET:
			return bits.getc();

		case DXF_LAYER_REF:
		case DXF_ENTITY_REF: {
			int	v = 0;
			for (int i = 0; i < 8; i++)
				v = (v << 4) | from_digit(bits.getc());
			return H(v);
		}
		case DXF_BINARY:
			return malloc_block(bits, bits.getc());

		case DXF_POINTS: case DXF_POINTS + 1: case DXF_POINTS + 2: case DXF_POINTS + 3:
			return RD3.get(bits);

		case DXF_REALS: case DXF_REALS + 1: case DXF_REALS + 2: 
			return bits.getDouble();

		case DXF_SHORT:
			return RS.get(bits);

		case DXF_LONG:
			return RL.get(bits);

		default:
		case DXF_INVALID:
			bits.seek_cur_bit(-8);
			return malloc_block(bits, remaining);
	}
}

dynamic_array<Variant> read_extended(bitsin bits, int size) {
	dynamic_array<Variant>	vars;

	bit_seeker	bs(bits, size * 8);
	while (bits.tell_bit() < bs.end)
		vars.push_back(read_extended1(bits, bs.end - bits.tell_bit()));

	return vars;
}
*/

int get_string_offset(bitsin bits, int bsize) {
	try(bit_seeker	bs = new bit_seeker(bits, 0)) {
		int	offset = 0;
		if (bits.ver(VER.R2007) >= 0) {
			bits.seek_bit(bsize - 1);
			if (bits.get_bit()) {
				bits.seek_bit(bsize - 17);
				int	ssize	= bits.get16();
				if ((ssize & 0x8000) != 0) {
					bits.seek_bit(bsize - 33);
					ssize = ((ssize & 0x7fff) + (bits.get16() << 15)) + 16;
				}
				offset = bsize - ssize - 17;
			}
		}
		return offset;
	}
}

//-----------------------------------------------------------------------------
// Object
//-----------------------------------------------------------------------------

static class Object {
	static final int no_xdict	= 1 << 16;
	static final int xdep		= 1 << 17;	//Object only
	static final int has_binary	= 1 << 18;	//Object only
	static final int has_entity	= 1 << 19;	//Object only

	OBJECTTYPE	type;
	int		flags			= 0;
	int		handle			= 0;
	int		parentH			= 0;	// Soft-pointer ID/handle to owner object

	H[]				reactors;
	Map<H, java.lang.Object> extended;

	Object() {
		// instantiate all bit_readable fields
		bit_readable.instantiate(this);
	}

	boolean common_head(bitsin bits) {
		type = OBJECTTYPE.get(bits);

		if (bits.ver(VER.R2000) >= 0 && bits.ver(VER.R2007) <= 0)
			bits.size = bits.get32();

		handle	= H.get(bits).offset();

		var	xsize = BS.get(bits).v;
		if (xsize != 0) {
			extended = new HashMap<H, java.lang.Object>();
			do {
				var	ah = H.get(bits);
				extended.put(ah, bits.readbuff(xsize));
				xsize = BS.get(bits).v;
			} while (xsize != 0);
		}
		
		return true;
	}
	boolean parse_head(bitsin bits) {
		common_head(bits);

		if (bits.ver(VER.R14) <= 0)
			bits.size = bits.get32();

		reactors = new H[BL.get(bits).v];

		if (bits.ver(VER.R2004) >= 0)
			flags |= bits.with_flag(no_xdict);

		if (bits.ver(VER.R2013) >= 0)
			flags |= bits.with_flag(has_binary);

		return true;
	}
	boolean parse_handles(bitsin bits) {
		if (bits.ver(VER.R2007) >= 0)		// skip string area
			bits.seek_bit(bits.size);

		parentH = H.get(bits).get_offset(handle);

		bits.read(reactors);

		if ((flags & no_xdict) == 0)//linetype in 2004 seems not have XDicObjH or NULL handle
			bits.read(new H());//XDicObjH

		return true;
	}
	boolean parse_handles(bitsin2 bits) {	// for verification
		//ISO_ASSERT(bits.ver < R2007 || bits.check_skip_strings());
		return parse_handles((bitsin)bits);
	}
	boolean parse(bitsin2 bits)	{ return parse_head(bits) && parse_handles(bits); }
};

//-----------------------------------------------------------------------------
//	Entity
//-----------------------------------------------------------------------------

class Entity extends Object {
	static final int entmode0		= 1 << 24;
	static final int entmode1		= 1 << 25;
	static final int no_next_links	= 1 << 26;
	static final int edge_vis_style	= 1 << 27;
	static final int face_vis_style	= 1 << 28;
	static final int full_vis_style	= 1 << 29;
	static final int invisible		= 1 << 30;
	static final int is_entity		= 1 << 31;

	static final byte BYLAYER		= 0;
	static final byte CONTINUOUS	= 1;
	static final byte BYBLOCK		= 2;
	static final byte HANDLE		= 3;

	LineWidth	lWeight			= LineWidth.widthByLayer;
	BD			linetypeScale	= new BD(1.0);
	ENC			color;

	byte		plot_flags		= BYLAYER;
	byte		line_flags		= BYLAYER;
	byte		material_flags	= BYLAYER;
	byte		shadow_flags;//0 both, 1 receives, 2 casts, 3 no

	//handles
	int		linetypeH;
	int		plotstyleH;
	int		materialH;
	int		shadowH;
	int		layerH;
	int		next_ent = 0;
	int		prev_ent = 0;

	byte[]	graphics_data;

	boolean parse_embedded_head(bitsin bits) {
		flags |= (bits.get_bits(2) * entmode0) | is_entity;

		reactors = new H[BL.get(bits).v];// ODA says BS

		if (bits.ver(VER.R14) <= 0)
			line_flags = bits.get_bit() ? BYLAYER : HANDLE;

		if (bits.ver(VER.R2004) >= 0)
			flags |= bits.with_flag(no_xdict);

		if (bits.ver(VER.R2007) <= 0)
			flags |= no_next_links;
		else
			flags |= bits.with_flag(no_next_links);

		bits.read(color);
		bits.read(linetypeScale);
		if (bits.ver(VER.R2000) >= 0) {
			line_flags = (byte) bits.get_bits(2);
			plot_flags = (byte) bits.get_bits(2);
		}
		if (bits.ver(VER.R2007) >= 0) {
			material_flags = (byte) bits.get_bits(2);
			shadow_flags = (byte) bits.getc();
		}
		if (bits.ver(VER.R2010) >= 0)
			flags |= bits.get_bits(3) * edge_vis_style;

		flags |= (BS.get(bits).v & 1) * invisible;// invisibleFlag(bits);

		if (bits.ver(VER.R2000) >= 0)
			lWeight = LineWidth.FromDWG(bits.getc());

		return true;
	}
	
	boolean parse_head(bitsin bits) {
		common_head(bits);

		if (bits.get_bit())
			graphics_data = bits.readbuff(bits.get32());

		if (bits.ver(VER.R14) <= 0)
			bits.size = bits.get32();

		return parse_embedded_head(bits);
	}
	boolean parse_handles(bitsin bits) {
		if (bits.ver(VER.R2007) >= 0)		// skip string area
			bits.seek_bit(bits.size);

		if ((flags & (entmode0 | entmode1)) == 0)	//entity is in block or polyline
			parentH = H.get(bits).get_offset(handle);

		bits.read(reactors);

		if ((flags & no_xdict) == 0)
			bits.read(new H());//XDicObjH

		if (bits.ver(VER.R14) <= 0) {
			layerH = H.get(bits).offset();
			if (line_flags == HANDLE)
				linetypeH = H.get(bits).offset();
		}
		if (bits.ver(VER.R2000) <= 0) {
			if ((flags & no_next_links) != 0) {
				next_ent = handle + 1;
				prev_ent = handle - 1;
			} else {
				prev_ent = H.get(bits).get_offset(handle);
				next_ent = H.get(bits).get_offset(handle);
			}
		}
		if (bits.ver(VER.R2004) >= 0) {
			//Parses Bookcolor handle
		}

		if (bits.ver(VER.R2000) >= 0) {
			layerH = H.get(bits).get_offset(handle);
			if (line_flags == HANDLE)
				linetypeH = H.get(bits).get_offset(handle);
		
			if (bits.ver(VER.R2007) >= 0) {
				if (material_flags == HANDLE)
					materialH = H.get(bits).get_offset(handle);
				if (shadow_flags == HANDLE)
					shadowH = H.get(bits).get_offset(handle);
			}
			if (plot_flags == HANDLE)
				plotstyleH = H.get(bits).get_offset(handle);
		}
		return true;
	}
	boolean parse_handles(bitsin2 bits) {	// for verification
		//ISO_ASSERT(bits.ver(VER.R2007) < 0 || bits.check_skip_strings());
		return parse_handles((bitsin)bits);
	}

	boolean parse(bitsin2 bits)	{
		 return parse_head(bits) && parse_handles(bits);
	}
};

//-----------------------------------------------------------------------------
//	entities
//-----------------------------------------------------------------------------

static double BDV(bit_reader bits) {
	return bits.ver(VER.R2000) >= 0 ? bits.getDouble() : BD.get(bits).v;
}

class DRW_TEXT extends Entity {

	RD2		insert_point, align_point;
	double	elevation;
	BEXT	ext_point;		// Dir extrusion normal vector, code 210, 220 & 230
	BT		thickness;		// thickness, code 39 *
	double	height;			// height text, code 40
	TV		text;			// text string, code 1
	double	angle;			// rotation angle in degrees (360), code 50
	double	widthscale;		// width factor, code 41
	double	oblique;		// oblique angle, code 51
	BS		textgen;		// text generation, code 71
	BS		alignH;			// horizontal align, code 72
	BS		alignV;			// vertical align, code 73
	H		styleH;			// e->style = textstyle_map.findname(e->styleH);

	boolean parse_head(bitsin2 bits) {
		if (!super.parse_head(bits))
			return false;

		int data_flags = bits.ver(VER.R2000) >= 0 ? bits.getc() : 0;

		if ((data_flags & 1) == 0)
			elevation =  BDV(bits);

		bits.read(insert_point);

		if (bits.ver(VER.R2000) >= 0) {
			if ((data_flags & 2) == 0) {
				align_point.x = DD.adjust(bits, insert_point.x);
				align_point.y = DD.adjust(bits, insert_point.y);
			}
		} else {
			bits.read(align_point);
		}

		bits.read(ext_point);
		bits.read(thickness);

		if ((data_flags & 4) == 0)
			oblique = BDV(bits);
		if ((data_flags & 8) == 0)
			angle = BDV(bits);
		height = BDV(bits);
		if ((data_flags & 16) == 0)
			widthscale = BDV(bits);

		bits.read(text);
		if ((data_flags & 0x20) == 0)
			bits.read(textgen);
		if ((data_flags & 0x40) == 0)
			bits.read(alignH);
		if ((data_flags & 0x80) == 0)
			bits.read(alignV);

		return true;
	}
	boolean parse_handles(bitsin bits) {
		return super.parse_handles(bits)
			&& bits.read(styleH);
	}
	public boolean parse(bitsin2 bits) {
		return parse_head(bits) && parse_handles(bits);
	}
};

class DRW_MTEXT extends Entity {
	//enum Attach {
	//	TopLeft = 1,
	//	TopCenter,
	//	TopRight,
	//	MiddleLeft,
	//	MiddleCenter,
	//	MiddleRight,
	//	BottomLeft,
	//	BottomCenter,
	//	BottomRight
	//};
	BD3		point1, point2;
	BD3		ext_point;		// Dir extrusion normal vector, code 210, 220 & 230
	BT		thickness;		// thickness, code 39
	BD		height;			// height text, code 40
	TV		text;			// text string, code 1
	BD		widthscale;		// width factor, code 41
	BD		oblique;		// oblique angle, code 51
	String	style;			// style name, code 7
	BS		textgen;		// text generation, code 71
	H		styleH;
	BS		draw_dir;		// Drawing dir BS 72 Left to right, etc.; see DXF doc
	BD		ext_ht;
	BD		ext_wid;
	BS		LinespacingStyle;
	BD		LinespacingFactor;
	CMC		col;

	boolean parse_head(bitsin2 bits) {
		if (!super.parse_head(bits))
			return false;

		bits.read(point1, ext_point, point2, widthscale);

		if (bits.ver(VER.R2007) >= 0)
			bits.read(new BD());		// Rect height BD 46 Reference rectangle height

		bits.read(height, textgen, draw_dir, ext_ht, ext_wid);

		bits.read(text);

		if (bits.ver(VER.R2000) >= 0) {
			bits.read(LinespacingStyle, LinespacingFactor);
			bits.get_bit();
		}
		if (bits.ver(VER.R2004) >= 0) {
			// Background flags BL 0 = no background, 1 = background fill, 2 =background fill with drawing fill color
			var bk_flags = BL.get(bits).v;
			switch (bk_flags) {
				case 1:
					BL.get(bits);
					col.read(bits);
					BL.get(bits);
					break;
				case 3:
					bits.seek_cur_bit(112);
					break;
			}
		}
		return true;
	}

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& parse_handles(bits)
			&& bits.read(styleH);
	}

	boolean parse_embedded(bitsin bits) {
		return parse_embedded_head(bits)
			&& parse_handles(bits)
			&& bits.read(styleH);
	}
};

class DRW_ATTRIB extends DRW_TEXT {
	static final int invisible		= 1 << 0;
	static final int constant		= 1 << 1;
	static final int verification	= 1 << 2;
	static final int preset			= 1 << 3;
	static final int lock			= 1 << 4;

	//enum ATTTYPE {
	//	Singleline =1,
	//	Multiline = 2,// (ATTRIB) 
	//	Multiline2 = 4,// (ATTDEF)
	//};
	byte[]		annotation;
	H			annotation_app;
	BS			annotation_short;
	DRW_MTEXT	mtext;
	TV			tag;
	BS			field_length;

	boolean parse_head(bitsin2 bits) {
		if (!super.parse_head(bits))
			return false;

		//SUBCLASS (AcDbAttribute)

		int	version		= bits.ver(VER.R2010) >= 0 ? bits.getc() : 0;
		int	att_type	= bits.ver(VER.R2018) >= 0 ? bits.getc() : 0;

		if (att_type > 1/*Singleline*/) {
			mtext	= new DRW_MTEXT();
			mtext.parse_embedded(bits);
			//MTEXT
		}

		annotation = new byte[BS.get(bits).v];
		if (annotation.length != 0)
			bits.read(annotation_app, annotation_short);

		bits.read(tag, field_length);

		flags	|= bits.getc();
		flags	|= bits.with_flag(lock);
		return true;
	}
	public boolean parse(bitsin2 bits) {
		return parse_head(bits) && parse_handles(bits);
	}
};

class DRW_ATTDEF extends DRW_ATTRIB {
	RC	version;
	TV		prompt;
	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(version, prompt)
			&& parse_handles(bits);
	}
};

class DRW_SHAPE extends Entity {
	BD3		ins_pt;
	BD		scale;
	BD		rotation;	//BD0?
	BD		width_factor;
	BD		oblique_angle;
	BD		thickness;	//DB0?
	BS		style_id; // STYLE index in dwg to SHAPEFILE
	BD3		extrusion;
	H		style;

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(ins_pt, scale, rotation, width_factor, oblique_angle, thickness, style_id, extrusion)
			&& parse_handles(bits)
			&& bits.read(style);
	}
};

class DRW_REGION				extends Entity {};
class DRW_SOLID_3D			extends Entity {};
class DRW_OLEFRAME			extends Entity {};
class DRW_TOLERANCE			extends Entity {};
class DRW_OLE2FRAME			extends Entity {};
class DRW_ACAD_PROXY_ENTITY	extends Entity {};
class DRW_MLEADER			extends Entity {};
class DRW_BODY extends Entity {};
class DRW_MLINE extends Entity {};

class DRW_POINT extends Entity {
	BD3		point;			// base point, code 10, 20 & 30
	BT		thickness;		// thickness, code 39
	BEXT	ext_point;		// Dir extrusion normal vector, code 210, 220 & 230
	BD		x_axis;			// Angle of the X axis for the UCS in effect when the point was drawn

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(point, thickness, ext_point, x_axis)
			&& parse_handles(bits);
	}
};

class DRW_LINE extends Entity {
	BD3		point1;
	BD3		point2;
	BT		thickness;
	BEXT	ext_point;

	boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		if (bits.ver(VER.R14) <= 0) {
			bits.read(point1);
			bits.read(point2);

		} else {
			boolean	zIsZero = bits.get_bit();
			point2.x.v	= DD.adjust(bits, point1.x.v = bits.getDouble());
			point2.y.v	= DD.adjust(bits, point1.y.v = bits.getDouble());
			if (!zIsZero)
				point2.z.v = DD.adjust(bits, point1.z.v = bits.getDouble());
		}
		bits.read(thickness);
		bits.read(ext_point);
		return parse_handles(bits);
	}
};

class DRW_RAY extends Entity {
	BD3		point1;
	BD3		point2;

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(point1, point2)
			&& parse_handles(bits);
	}
};

class DRW_XLINE extends DRW_RAY {};

class DRW_CIRCLE extends Entity {
	BD3		centre;
	BD		radius;
	BT		thickness;
	BEXT	ext_point;

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(centre, radius, thickness, ext_point)
			&& parse_handles(bits);
	}
};

class DRW_ARC extends DRW_CIRCLE {
	BD		angle0, angle1;	// start/end angles in radians

	boolean parse(bitsin2 bits) {
		return super.parse(bits)
			&& bits.read(angle0, angle1)
			&& parse_handles(bits);
	}
};

class DRW_ELLIPSE extends Entity {
	BD3		point1, point2;
	BD3		ext_point;
	BD		ratio;
	BD		angle0, angle1;	// start/end angles in radians

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(point1, point2, ext_point, ratio, angle0, angle1)
			&& parse_handles(bits);
	}
};

class DRW_TRACE extends Entity	{
	BT		thickness;
	BD3		point1, point2, point3, point4;
	BEXT	ext_point;

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(thickness, point1, point2, point3, point4, ext_point)
			&& parse_handles(bits);
	}
};

class DRW_SOLID extends DRW_TRACE {};

class DRW_FACE_3D extends DRW_TRACE {
	BD3		point1, point2, point3, point4;
	BS		invisibleflag;	// bit per edge

	boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		if (bits.ver(VER.R14) <= 0 ) {// R13 & R14
			if (!bits.read(point1, point2, point3, point4, invisibleflag))
				return false;

		} else { // 2000+
			boolean has_no_flag	= bits.get_bit();
			boolean z_is_zero	= bits.get_bit();
			point1.x.v	= bits.getDouble();
			point1.y.v	= bits.getDouble();
			point1.z.v	= z_is_zero ? 0.0 : bits.getDouble();
			//DD(bits, (double*)&point2, (const double*)&point1, 3);
			//DD(bits, (double*)&point3, (const double*)&point2, 3);
			//DD(bits, (double*)&point4, (const double*)&point3, 3);
			if (!has_no_flag)
				bits.read(invisibleflag);
		}

		return parse_handles(bits);
	}
};

class DRW_BLOCK extends Entity {
	TV		name;//	= "*U0";
//	BD3		base_point;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.read(name);
		return parse_handles(bits);
	}
};

class DRW_ENDBLK extends Entity {
	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		if (bits.ver(VER.R2007) >= 0)
			bits.get_bit();

		return parse_handles(bits);
	}
};

class DRW_SEQEND extends Entity {};


class DRW_INSERT extends Entity {
	BD3		base_point;
	BD3		ext_point;
	BSCALE	scale;
	BD		angle;
	H		block;
	HandleRange	handles;

	boolean parse_handles(bitsin bits, int count) {
		return super.parse_handles(bits)
			&& bits.read(block)
			&& handles.read(bits, count);
	}

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(base_point, scale, angle, ext_point)
			&& parse_handles(bits, !bits.get_bit() ? -1 : bits.ver(VER.R2004) >= 0 ? BL.get(bits).v : 1);
	}
	public HandleCollection children() {
		return new HandleCollection(handles, handle);
	}
};

class DRW_MINSERT extends DRW_INSERT {
	BS colcount, rowcount;
	BD colspace, rowspace;

	public boolean parse(bitsin2 bits) {
		if (!super.parse(bits) || !bits.read(base_point, scale, angle, ext_point))
			return false;

		int	count = !bits.get_bit() ? 0 : bits.ver(VER.R2004) >= 0 ? BL.get(bits).v : 2;
		return bits.read(colcount, rowcount, colspace, rowspace)
			&& parse_handles(bits, count);
	}
};

class DRW_LWPOLYLINE extends Entity {
	static final int has_ext	= 1 << 0;
	static final int has_thick	= 1 << 1;
	static final int has_width	= 1 << 2;
	static final int has_elev	= 1 << 3;
	static final int has_bulges	= 1 << 4;
	static final int has_widths	= 1 << 5;
	static final int plinegen	= 1 << 7;
	static final int open		= 1 << 9;
	static final int has_ids	= 1 << 10;
	BD		width;			// constant width, code 43
	BD		elevation;		// elevation, code 38
	BD		thickness;		// thickness, code 39
	BEXT	ext_point;		// Dir extrusion normal vector, code 210, 220 & 230

	class Vertex {
		double	x, y;
		double	width0 = 0, width1 = 0;
		double	bulge = 0;
		int		id = 0;
		Vertex(double _x, double _y) { x = _x; y = _y; }
	};

	Vertex[]	vertlist;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		flags |= BS.get(bits).v;
		if ((flags & has_width) != 0)	bits.read(width);
		if ((flags & has_elev) != 0)	bits.read(elevation);
		if ((flags & has_thick) != 0)	bits.read(thickness);
		if ((flags & has_ext) != 0)		bits.read(ext_point);

		int	vertexnum 		= BL.get(bits).v;
		int	bulge_count		= ((flags & has_bulges) != 0) ? BL.get(bits).v : 0;
		int	id_count		= ((flags & has_ids) 	!= 0) ? BL.get(bits).v : 0;
		int	widths_count	= ((flags & has_widths) != 0) ? BL.get(bits).v : 0;

		vertlist = new Vertex[vertexnum];

		double px = 0, py = 0;
		for (int i = 0; i < vertexnum; i++) {
			if (i == 0 || bits.ver(VER.R14) <= 0) {//14-
				px = bits.getDouble();
				py = bits.getDouble();
			} else {
				px = DD.adjust(bits, px);
				py = DD.adjust(bits, py);
			}
			vertlist[i] = new Vertex(px, py);
		}

		//add bulges
		for (int i = 0; i < bulge_count; i++) {
			double bulge = BD.get(bits).v;
			if (i < vertexnum)
				vertlist[i].bulge = bulge;
		}
		//add vertexId
		for (int i = 0; i < id_count; i++) {
			int id = BL.get(bits).v;
			if (i < vertexnum)
				vertlist[i].id = id;
		}
		//add widths
		for (int i = 0; i < widths_count; i++) {
			double w0 = BD.get(bits).v, w1 = BD.get(bits).v;
			if (i < vertexnum) {
				vertlist[i].width0 = w0;
				vertlist[i].width1 = w1;
			}
		}
		return parse_handles(bits);
	}
};

class DRW_VERTEX_PFACE_FACE extends Entity {
	BS[]	index = new BS[4];		// polyface mesh vertex indices

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(index)
			&& parse_handles(bits);
	}
};

class DRW_VERTEX_2D extends Entity {
	BD3		point;
	BD		width0, width1;
	BD		bulge;			// bulge, code 42
	BL		id;
	BD		tgdir;			// curve fit tangent direction, code 50

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		flags		|= bits.getc();
		bits.read(point);
		bits.read(width0);
		if (width0.v < 0)
			width1.v = width0.v = Math.abs(width0.v);
		else
			bits.read(width1);
		bits.read(bulge);
		if (bits.ver(VER.R2010) >= 0) 
			bits.read(id);
		bits.read(tgdir);

		return parse_handles(bits);
	}
};

// VERTEX_3D, VERTEX_MESH, VERTEX_PFACE
class Vertex extends Entity {
	BD3		point;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		flags	|= bits.getc();
		return bits.read(point) && parse_handles(bits);
	}
};

class DRW_VERTEX_3D extends Vertex {};
class DRW_VERTEX_MESH extends Vertex {};
class DRW_VERTEX_PFACE extends Vertex {};

class Polyline extends Entity {
	HandleRange		handles;
	Entity[]		vertices;

	boolean parse_handles(bitsin bits) {
		int count = bits.ver(VER.R2004) >= 0 ? BL.get(bits).v : 1;

		if (!super.parse_handles(bits))
			return false;

		handles.read(bits, count);
		return true;
	}
	
	public HandleCollection children() {
		return new HandleCollection(handles, handle);
	}
};

class DRW_POLYLINE_2D extends Polyline {
	BS		curve_type;		// curves & smooth surface type, code 75, default 0
	BD		width0;
	BD		width1;
	BT		thickness;
	BD		elevation;
	BEXT	ext_point;

	public boolean parse(bitsin2 bits) {
		BS		tflags = new BS();
		if (parse_head(bits) && bits.read(tflags, curve_type, width0, width1, thickness, elevation, ext_point))
			flags |= tflags.v;
		return parse_handles(bits);
	}
};

class DRW_POLYLINE_3D extends Polyline {
	static final int curve_type_mask	= 3 << 0;
	static final int someflag			= 1 << 2;

	byte	curve_type;		// curves & smooth surface type, code 75, default 0

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		flags |= bits.getc();
		flags |= (bits.getc() & 1) << someflag;

		return parse_handles(bits);
	}
};

class DRW_POLYLINE_PFACE extends Polyline {
	BS		vertexcount;		// polygon mesh M vertex or polyface vertex num, code 71, default 0
	BS		facecount;			// polygon mesh N vertex or polyface face num, code 72, default 0

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(vertexcount, facecount)
			&& parse_handles(bits);
	}
};

class DRW_POLYLINE_MESH extends Polyline {
	BS		curve_type;
	BS		num_m_verts;
	BS		num_n_verts;
	BS		m_density;
	BS		n_density;

	public boolean parse(bitsin2 bits) {
		parse_head(bits);
		BS		tflags = new BS();
		bits.read(tflags, curve_type, num_m_verts, num_n_verts, m_density, n_density);
			flags |= tflags.v;

		int	count = bits.ver(VER.R2004) >= 0 ? BL.get(bits).v : 0;
		return parse_handles(bits) && handles.read(bits, count);
	}
};

class DRW_SPLINE extends Entity {
	static final int periodic	= 1 << 0;
	static final int closed		= 1 << 1;
	static final int rational	= 1 << 2;

	BD3		tangent0;
	BD3		tangent1;
	BL		degree;			// degree of the spline, code 71
	BD		knot_tol;		// knot tolerance, code 42, default 0.0000001
	BD		control_tol;	// control point tolerance, code 43, default 0.0000001
	BD		fit_tol;		// fit point tolerance, code 44, default 0.0000001

	BD[]	knotslist;			// knots list, code 40
	BD[]	weightlist;			// weight list, code 41
	BD3[]	controllist;		// control points list, code 10, 20 & 30
	BD3[]	fitlist;			// fit points list, code 11, 21 & 31

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		int scenario = BL.get(bits).v;
		if (bits.ver(VER.R2013) >= 0) {
			if ((BL.get(bits).v & 1) != 0)
				scenario = 2;
			BL.get(bits);
		}
		bits.read(degree);

		BL		nknots	= new BL();		// number of knots, code 72, default 0
		BL		ncontrol = new BL();	// number of control points, code 73, default 0
		BL		nfit = new BL();		// number of fit points, code 74, default 0
		B		weight = new B();		// RLZ ??? flags, weight, code 70, bit 4 (16)

		if (scenario == 2) {
			bits.read(fit_tol, tangent0, tangent1, nfit);
		} else if (scenario == 1) {
			flags	|= bits.get_bits(3);
			bits.read(knot_tol, control_tol, nknots, ncontrol, weight);
		} else {
			return false; //RLZ: from doc only 1 or 2 are ok ?
		}

		bits.read(knotslist = new BD[nknots.v]);
		
		controllist = new BD3[ncontrol.v];
		if (weight.v)
			weightlist = new BD[ncontrol.v];

		for (int i = 0; i < ncontrol.v; ++i) {
			bits.read(controllist[i] = new BD3());
			if (weight.v)
				bits.read(weightlist[i] = new BD()); //RLZ Warning: D (BD or RD)
		}
		bits.read(fitlist = new BD3[nfit.v]);
		return parse_handles(bits);
	}
};

static class HatchLoop {
	static final int  VERTEX		= -1;
	static final int  LINE			= 1;
	static final int  CIRCLE_ARC	= 2;
	static final int  ELLIPSE_ARC	= 3;
	static final int  SPLINE		= 4;

	static class Item {
		int	type;
		Item(int _type) { type = _type; bit_readable.instantiate(this); }
	};
	
	static class Vertex extends Item {
		RD2	point;
		BD	bulge;
		Vertex(bit_reader bits, boolean has_bulge) { super(VERTEX); bits.read(point); if(has_bulge) bits.read(bulge); }
	};
	
	static class Line extends Item {
		RD2	point1, point2;
		Line(bit_reader bits) { super(LINE); bits.read(point1, point2); }
	};
	
	static class CircleArc extends Item {
		RD2	centre;
		BD	radius, angle0, angle1;
		B	isccw;
		CircleArc(bit_reader bits) { super(CIRCLE_ARC); bits.read(centre, radius, angle0, angle1, isccw); }
	};
	
	static class EllipseArc extends Item {
		RD2	point1, point2;
		BD	ratio, param0, param1;
		B	isccw;
		EllipseArc(bit_reader bits) { super(ELLIPSE_ARC); bits.read(point1, point2, ratio, param0, param1, isccw); }
	};
	
	static class Spline extends Item {
		BL	degree;
		B	isRational, periodic;
		RD2	tangent0, tangent1;
		RD[]		knotslist;
		RD3[]		controllist;
		RD2[]		fitlist;

		Spline(bit_reader bits) {
			super(SPLINE);
			bits.read(degree, isRational, periodic);
			int	nknots	= BL.get(bits).v, ncontrol = BL.get(bits).v;

			bits.read(knotslist = new RD[nknots]);

			controllist = new RD3[ncontrol];
			for (int j = 0; j < ncontrol;++j) {
				double	x = bits.getDouble(), y= bits.getDouble(), z = isRational.v ? bits.getDouble() : 0;
				controllist[j] = new RD3(x, y, z);
			}
			if (bits.ver(VER.R2010) >= 0) { 
				bits.read(fitlist = new RD2[BL.get(bits).v]);
				bits.read(tangent0);
				bits.read(tangent1);
			}
		}
	};

	int		type;	// boundary path type, code 92, polyline=2, default=0
	boolean	closed;	// only polyline
	Item[]	objlist;

	HatchLoop(bit_reader bits) {
		type = BL.get(bits).v;
		if ((type & 2) == 0) {
			objlist = new Item[BL.get(bits).v];
			for (int j = 0; j < objlist.length; ++j) {
				switch (bits.getc()) {
					case LINE:			objlist[j] = new Line(bits);		break;
					case CIRCLE_ARC:	objlist[j] = new CircleArc(bits);	break;
					case ELLIPSE_ARC:	objlist[j] = new EllipseArc(bits);	break;
					case SPLINE:		objlist[j] = new Spline(bits);		break;
				}
			}
		} else {
			var	has_bulge = bits.get_bit();
			closed	= bits.get_bit();
			objlist = new Item[BL.get(bits).v];
			for (int j = 0; j < objlist.length; ++j)
				objlist[j] = new Vertex(bits, has_bulge);
		}
	}
};

static class HatchLine implements bit_readable {
	BD	angle;
	BD2 point, offset;
	BD[]	dash;
	public boolean read(bit_reader bits) {
		bit_readable.instantiate(this);
		return bits.read(angle, point, offset) && bits.read(dash = new BD[BS.get(bits).v]);
	}
};

class DRW_HATCH extends Entity {
	static final int associative	= 1 << 0;
	static final int solid			= 1 << 1;
	static final int use_double		= 1 << 2;

	TV		name;
	BD		elevation;
	BD3		ext_point;
	BS		hstyle;			// hatch style, code 75
	BS		hpattern;		// hatch pattern type, code 76
	BD		angle;			// hatch pattern angle, code 52
	BD		scale;			// hatch pattern scale, code 41
	BD		pixsize;

	HatchLine[]	deflines;	// pattern definition lines
	HatchLoop[] loops;		// polyline list
	TV			grad_name;
	Gradient	grad;
	RD2[]		seeds;
	H[]			bound;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		if (bits.ver(VER.R2004) >= 0) { 
			grad.read(bits);
			bits.read(grad_name);
		}
		bits.read(elevation, ext_point, name);
		flags	|= bits.get_bits(2);

		int	numloops = BL.get(bits).v, total_bound = 0;
		loops = new HatchLoop[numloops];
		boolean have_pixel_size = false;
		for (int i = 0; i < numloops; i++) {
			loops[i] = new HatchLoop(bits);
			have_pixel_size	|= (loops[i].type & 4) != 0;
			total_bound += BL.get(bits).v;
		}

		bits.read(hstyle, hpattern);

		if ((flags & solid) == 0) {
			bits.read(angle, scale);
			flags	|= bits.with_flag(use_double);
			bits.read(deflines = new HatchLine[BS.get(bits).v]);
		}

		if (have_pixel_size)
			bits.read(pixsize);
		
		bits.read(seeds = new RD2[BL.get(bits).v]);

		if (!parse_handles(bits))
			return false;

		bits.read(bound = new H[total_bound]);
		return true;
	}
};

class DRW_IMAGE extends Entity {
	static final int	clip_mode	= 1 << 0;

	BD3		point1;
	BD3		point2;
	BD3		v_vector;		// V-vector of single pixel, x coordinate, code 12, 22 & 32
	RD2		size;			// image size in pixels, U value, code 13
	B		clip;			// Clipping state, code 280, 0=off 1=on
	RC		brightness;		// Brightness value, code 281, (0-100) default 50
	RC		contrast;		// Brightness value, code 282, (0-100) default 50
	RC		fade;			// Brightness value, code 283, (0-100) default 0
	H		ref;			// Hard reference to imagedef object, code 340

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		BL	classVersion = new BL();
		BS	displayProps = new BS();
		bits.read(classVersion, point1, point2, v_vector, size, displayProps, clip, brightness, contrast, fade);

		if (bits.ver(VER.R2010) >= 0) 
			flags |= bits.with_flag(clip_mode);

		int clipType = BS.get(bits).v;
		if (clipType == 1) {
			bits.read(new RD(), new RD());

		} else { //clipType == 2
			for (int i = 0, n = BL.get(bits).v; i < n; ++i)
				bits.getDouble();
		}

		if (!parse_handles(bits))
			return false;

		bits.read(ref);
		H.get(bits);
		return true;
	}
};

class Dimension extends Entity {
	static final int non_default	= 1 << 0;
	static final int use_block		= 1 << 1;
	static final int flip_arrow1	= 1 << 2;
	static final int flip_arrow2	= 1 << 3;
	static final int has_arrow2		= 1 << 4;
	static final int unknown		= 1 << 6;

	RC		class_version;
	BD3		extrusion;
	RD2		text_midpt;
	BD		elevation;
	TV		user_text;
	BD		text_rotation;
	BD		horiz_dir;
	BD3		ins_scale;
	BD		ins_rotation;
	BS		attachment;
	BS		lspace_style;
	BD		lspace_factor;
	BD		act_measurement;
	RD2		clone_ins_pt;

	H		styleH;
	H		blockH;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		if (bits.ver(VER.R2010) >= 0)
			bits.read(class_version);

		RC	tflags = new RC();
		bits.read(extrusion, text_midpt, elevation, tflags, user_text, text_rotation, horiz_dir, ins_scale, ins_rotation);
		flags |= tflags.v & 0x43;

		if (bits.ver(VER.R2000) >= 0)
			bits.read(attachment, lspace_style, lspace_factor, act_measurement);

		if (bits.ver(VER.R2007) >= 0)
			flags |= bits.get_bits(3) * flip_arrow1;

		return bits.read(clone_ins_pt);
	}
	boolean parse_handles(bitsin bits) {
		if (!super.parse_handles(bits))
			return false;

		bits.read(styleH);
		bits.read(blockH);
		return true;
	}
};


class DRW_DIMENSION_ORDINATE extends Dimension {
	BD3	defpoint;
	BD3	def1;
	BD3	def2;
	public boolean parse(bitsin2 bits) {
		RC	type2 = new RC();
		//type =  (type2 & 1) ? type | 0x80 : type & 0xBF; //set bit 6
		return super.parse(bits)
			&&	bits.read(defpoint, def1, def2, type2)
			&&	parse_handles(bits);
	}
};

class DRW_DIMENSION_LINEAR extends Dimension {
	BD3	defpoint;
	BD3	def1;
	BD3	def2;
	BD	oblique;
	BD	angle;

	public boolean parse(bitsin2 bits) {
		return super.parse(bits)
			&&	bits.read(def1, def2, defpoint, oblique, angle)
			&&	parse_handles(bits);
	}
};

class DRW_DIMENSION_ALIGNED extends Dimension {
	BD3	defpoint;
	BD3	def1;
	BD3	def2;
	BD	oblique;

	public boolean parse(bitsin2 bits) {
		return super.parse(bits)
			&&	bits.read(def1, def2, defpoint, oblique)
			&&	parse_handles(bits);
	}
};

class DRW_DIMENSION_ANG_LN2 extends Dimension {
	RD2	arcPoint;
	BD3	def1;
	BD3	def2;
	BD3	centrePoint;
	BD3	defpoint;

	public boolean parse(bitsin2 bits) {
		return super.parse(bits)
			&&	bits.read(arcPoint, def1, def2, centrePoint, defpoint)
			&&	parse_handles(bits);
	}
};

class DRW_DIMENSION_ANG_PT3 extends Dimension {
	BD3	defpoint;
	BD3	def1;
	BD3	def2;
	BD3	centrePoint;

	public boolean parse(bitsin2 bits) {
		return super.parse(bits)
			&&	bits.read(defpoint, def1, def2, centrePoint)
			&&	parse_handles(bits);
	}
};

class DRW_DIMENSION_RADIUS extends Dimension {
	BD3	defpoint;
	BD3	circlePoint;
	BD	radius;

	public boolean parse(bitsin2 bits) {
		return super.parse(bits)
			&&	bits.read(defpoint, circlePoint, radius)
			&&	parse_handles(bits);
	}
};

class DRW_DIMENSION_DIAMETER extends Dimension {
	BD3	circlePoint;
	BD3	defpoint;
	BD	radius;

	public boolean parse(bitsin2 bits) {
		return super.parse(bits)
			&&	bits.read(circlePoint, defpoint, radius)
			&&	parse_handles(bits);
	}
};

class DRW_LEADER extends Entity {
	static final int arrow		= 1 << 0;
	static final int hook_dir	= 1 << 1;
	BD2		textsize;			// Text annotation height, code 40
	BEXT	extrusionPoint;		// Normal vector, code 210, 220 & 230
	BD3		horizdir;			// "Horizontal" direction for leader, code 211, 221 & 231
	BD3		offsetblock;		// Offset of last leader vertex from block, code 212, 222 & 232
	RD3		offsettext;			// Offset of last leader vertex from annotation, code 213, 223 & 233

	BD3[]	vertexlist;			// vertex points list, code 10, 20 & 30

	H		styleH;
	H		AnnotH;
	
	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.get_bit();//unknown_bit(bits);
		BS	annot_type	= BS.get(bits);
		BS	Path_type	= BS.get(bits);

		bits.read(vertexlist = new BD3[BL.get(bits).v]);

		var	Endptproj = BD3.get(bits);
		bits.read(extrusionPoint);
		if (bits.ver(VER.R2000) >= 0) 
			bits.get_bits(5);

		bits.read(horizdir);
		bits.read(offsetblock);
		if (bits.ver(VER.R14) >= 0)
			BD3.get(bits);

		if (bits.ver(VER.R14) <= 0) //R14 -
			BD.get(bits);//dimgap;

		if (bits.ver(VER.R2007) <= 0)
			bits.read(textsize);

		flags |= bits.get_bits(2);

		if (bits.ver(VER.R14) <= 0) {
			var	nArrow_head_type= BS.get(bits);
			var	dimasz			= BD.get(bits);
			bits.get_bits(2);//nunk_bit, unk_bit
			var	unk_short		= BS.get(bits);
			var	byBlock_color	= BS.get(bits);
		} else {
			BS.get(bits);
		}
		bits.get_bits(2);

		if (!parse_handles(bits))
			return false;

		bits.read(AnnotH, styleH);
		return true;
	}
};

class DRW_VIEWPORT extends Entity {
	static final int ucs_per_viewport	= 1 << 6;
	static final int ucs_at_origin		= 1 << 7;

	BD3		point;
	BD2		pssize;			// Width in paper space units, code 40
	RD2		centerP;		// view center point X, code 12
	RD2		snapP;			// Snap base point X, code 13
	RD2		snapSpP;		// Snap spacing X, code 14
	int		vpstatus;		// Viewport status, code 68
	int		vpID;			// Viewport ID, code 69

	BD3		view_target;		// View target point, code 17, 27, 37
	BD3		view_dir;		// View direction vector, code 16, 26 & 36
	BD		twist_angle;	// view twist angle, code 51
	BD		view_height;	// View height in model space units, code 45
	BD		view_length;	// Perspective lens length, code 42
	BD		front_clip;		// Front clip plane Z value, code 43
	BD		back_clip;		// Back clip plane Z value, code 44
	BD		snap_angle;		// Snap angle, code 50

	BS			grid_major;
	RenderMode	render_mode;
	UserCoords	ucs;
	H[]			frozen;

	H	vport_entity_header, clip_boundary, named_ucs, base_ucs;
	H	background, visualstyle, shadeplot, sun;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.read(point, pssize);

		if (bits.ver(VER.R2000) >= 0)
			bits.read(view_target, view_dir, twist_angle, view_height, view_length, front_clip, back_clip, snap_angle, centerP, snapP, snapSpP);
		if (bits.ver(VER.R2007) >= 0)
			bits.read(grid_major);

		if (bits.ver(VER.R2000) >= 0) {
			frozen	= new H[BL.get(bits).v];
			BL		status_flags	= BL.get(bits);
			TV		style_sheet		= TV.get(bits);
			int		render_mode		= bits.getc();
			flags	|= bits.get_bits(2) * ucs_per_viewport;
			ucs.read(bits);
		}
		if (bits.ver(VER.R2004) >= 0)
			render_mode.read(bits);

		if (!parse_handles(bits))
			return false;

		if (bits.ver(VER.R14) <= 0)
			bits.read(vport_entity_header);

		if (bits.ver(VER.R2000) >= 0) {
			for (var h : frozen)
				bits.read(h);
			bits.read(clip_boundary);

			if (bits.ver(VER.R2000) == 0)
				bits.read(vport_entity_header);
			
			bits.read(named_ucs, base_ucs);
		}
		if (bits.ver(VER.R2007) >= 0)
			bits.read(background, visualstyle, shadeplot, sun);

		return true;
	}
};

//-----------------------------------------------------------------------------
// Objects
//-----------------------------------------------------------------------------

static class ObjControl extends Object {
	int[]	handles;

	boolean parse_handles(bitsin bits, int n) {
		if (!super.parse_handles(bits))
			return false;

		handles = new int[n];
		for (int i = 0; i < n; i++)
			handles[i] = H.get(bits).get_offset(handle);

		return true;
	}

	boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		return parse_handles(bits, BL.get(bits).v);
	}
};

class DRW_BLOCK_CONTROL_OBJ		extends ObjControl {
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& parse_handles(bits, BL.get(bits).v + 2);
	}
};

class DRW_LTYPE_CONTROL_OBJ extends ObjControl {
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& parse_handles(bits, BL.get(bits).v + 2);
	}
};

class DRW_DIMSTYLE_CONTROL_OBJ extends ObjControl {
	boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		int	n = BL.get(bits).v;

		// V2000 dimstyle seems have one unknown byte hard handle counter??
		int unkData = bits.ver(VER.R2000) >= 0 ? (int) bits.getc() : 0;

		parse_handles(bits, n);
		for (int i = 0; i < unkData; i++)
			H.get(bits).get_offset(handle);
		return true;
	}
};

class DRW_LAYER_CONTROL_OBJ		extends ObjControl {};
class DRW_STYLE_CONTROL_OBJ		extends ObjControl {};
class DRW_VIEW_CONTROL_OBJ		extends ObjControl {};
class DRW_UCS_CONTROL_OBJ		extends ObjControl {};
class DRW_VPORT_CONTROL_OBJ		extends ObjControl {};
class DRW_APPID_CONTROL_OBJ		extends ObjControl {};
class DRW_VP_ENT_HDR_CTRL_OBJ	extends ObjControl {};

class DRW_DICTIONARY extends ObjControl {
	static final int cloning = 1 << 0;

	TV name;

	int parse0(bitsin2 bits) {
		if (!parse_head(bits))
			return -1;

		int n = BL.get(bits).v;
		if (bits.ver(VER.R14) <= 0) {
			bits.getc();
		} else {
			flags |= bits.with_flag(cloning);
			bits.getc();// hardowner
		}

		bits.read(name);
		return n;
	}

	public boolean parse(bitsin2 bits) {
		var n = parse0(bits);
		return n >= 0 && parse_handles(bits, n);
	}
};

class DRW_DICTIONARYWDFLT extends DRW_DICTIONARY {
	H def;

	public boolean parse(bitsin2 bits) {
		var n = parse0(bits);
		if (n < 0 || !parse_handles(bits, n))
			return false;

		bits.read(def);
		return true;
	}
};

class DRW_ACDBDICTIONARYWDFLT extends DRW_DICTIONARYWDFLT {
};

static class NamedObject extends Object {
	TV			name;

	boolean parse_head(bitsin2 bits) {
		if (!super.parse_head(bits))
			return false;

		bits.read(name);

		flags	|= bits.with_flag(has_entity);
		if (bits.ver(VER.R2004) <= 0)
			BS.get(bits);// xrefindex
		
		flags	|= bits.with_flag(xdep);
		return true;
	}
};

class DRW_BLOCK_HEADER extends NamedObject {
	static final int xrefOverlaid	= 1 << 0;
	static final int blockIsXref	= 1 << 1;
	static final int has_attdefs	= 1 << 2;
	static final int anonymous		= 1 << 3;
	static final int loaded_Xref	= 1 << 4;
	static final int can_explode	= 1 << 5;
	
	BS			insUnits;		// block insertion units, code 70 of block_record
	RC			scaling;
	BD3			base_point;		// block insertion base point dwg only
	TV			xref_path;
	TV			description;
	byte[]		preview;
	int			block;			// handle for block entity
	HandleRange	entities;
	H[] 		inserts;
	H			layoutH;

	boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		flags	|= bits.get_bits(4);
		if (bits.ver(VER.R2000) >= 0)
			flags	|= bits.with_flag(loaded_Xref);

		//Number of objects owned by this block
		int		objectCount = bits.ver(VER.R2004) >= 0 ? BL.get(bits).v : (flags & blockIsXref) == 0 && (flags & xrefOverlaid) == 0 ? 1 : -1;

		bits.read(base_point, xref_path);

		int insertCount = 0;
		if (bits.ver(VER.R2000) >= 0) {
			for (int i; (i = bits.getc()) != 0;)
				insertCount += i;
			bits.read(description);
			preview = bits.readbuff(BL.get(bits).v);
		}

		if (bits.ver(VER.R2007) >= 0) {
			bits.read(insUnits);
			flags		|= bits.with_flag(can_explode);
			bits.read(scaling);
		}

		parse_handles(bits);
		H.get(bits);//XRefH

		block		= H.get(bits).get_offset(handle);
		entities.read(bits, objectCount);

		if (bits.ver(VER.R2000) >= 0) {
			bits.read(inserts = new H[insertCount]);
			bits.read(layoutH);
		}
		return true;
	}
	
	public HandleCollection children() {
		return new HandleCollection(entities, handle);
	}
	//public Iterator<Entity>	iterator() {
	//	var	block2	= get_object(block);
//
	//	if (block2.parentH == 0) {
	//		// in dwg code 330 are not set like dxf in ModelSpace & PaperSpace, set it and do not send block entities like dxf
	//		block2.parentH = handle;
	//		//return new Entity[0];
	//	}
	//	return (new HandleCollection(entities, handle)).iterator();
	//}
};

class DRW_LAYER extends NamedObject {
	static final int frozen		= 1 << 0;
	static final int layeron	= 1 << 1;
	static final int frozen_new	= 1 << 2;
	static final int locked		= 1 << 3;
	static final int plotF		= 1 << 4;

	CMC			color;
	LineWidth	lWeight;
	H			plotstyleH;			// Hard-pointer ID/handle of plotstyle, code 390
	H			materialstyleH;		// Hard-pointer ID/handle of materialstyle, code 347
	H			linetypeH;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		if (bits.ver(VER.R14) <= 0) {
			flags |= bits.with_flag(frozen);
			bits.get_bit(); //unused, negate the color
			flags |= bits.with_flag(frozen_new);
			flags |= bits.with_flag(locked);
		} else {
			int f = BS.get(bits).v;
			flags |= f & 31;
			lWeight = LineWidth.FromDWG((f >> 5) & 31);
		}
		color.read(bits);

		parse_handles(bits);
		H.get(bits);//XRefH

		if (bits.ver(VER.R2000) >= 0)
			bits.read(plotstyleH);

		if (bits.ver(VER.R2007) >= 0)
			bits.read(materialstyleH);

		bits.read(linetypeH);
		return true;
	}
};

class DRW_STYLE extends NamedObject {
	static final int shape		= 1 << 0;
	static final int vertical	= 1 << 1;

	BD		height;		// Fixed text height (0 not set), code 40
	BD		width;		// Width factor, code 41
	BD		oblique;	// Oblique angle, code 50
	RC		genFlag;	// Text generation flags, code 71
	BD		lastHeight;	// Last height used, code 42
	TV		font;		// primary font file name, code 3
	TV		bigFont;	// bigfont file name or blank if none, code 4

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		flags	|= bits.get_bits(2);

		bits.read(height, width, oblique, genFlag, lastHeight);
		bits.read(font);
		bits.read(bigFont);

		parse_handles((bitsin)bits);//avoid test
		H.get(bits);//XRefH
		return true;
	}
};

static class LTYPE_Entry implements bit_readable {
	static final int horizontal		= 1 << 0;	// text is rotated 0 degrees, otherwise it follows the segment
	static final int shape_index	= 1 << 1;	// complexshapecode holds the index of the shape to be drawn
	static final int text_index		= 1 << 2;	// complexshapecode holds the index into the text area of the string to be drawn.

	BD	hash_length;
	BS	code;
	RD	x_offset, y_offset;
	BD	scale, rotation;
	BS	flags;
	public boolean	read(bit_reader bits) {
		bit_readable.instantiate(this);
		return bits.read(hash_length, code, x_offset, y_offset, scale, rotation, flags);
	}
};
class DRW_LTYPE extends NamedObject {
	TV		desc;					// descriptive string, code 3
	RC		align;					// align code, always 65 ('A') code 72
	BD		length;					// total length of pattern, code 40
	RC		haveShape;				// complex linetype type, code 74
	byte[]	strarea;
	H		dashH, shapeH;
	LTYPE_Entry[] path;	// trace, point or space length sequence, code 49

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.read(desc);
		bits.read(length);
		bits.read(align);

		bits.read(path = new LTYPE_Entry[bits.getc()]);
		boolean haveStrArea = false;
		for (var i : path)
			haveStrArea = haveStrArea || (i.flags.v & LTYPE_Entry.shape_index) != 0;

		if (bits.ver(VER.R2004) <= 0) 
			strarea = bits.readbuff(256);
		else if (haveStrArea)
			strarea = bits.readbuff(512);

		parse_handles(bits);
		if (path.length != 0) {
			H.get(bits);//XRefH
			bits.read(dashH);
		}
		bits.read(shapeH);
		return true;
	}
};

class DRW_VIEW extends NamedObject {
	static final int pspace		= 1 << 0;
	static final int plottable	= 1 << 1;

	BD		height;
	BD		width;
	RD2		center;

	BD3		view_target;		// View target point, code 17, 27, 37
	BD3		view_dir;		// View direction vector, code 16, 26 & 36
	BD		twist_angle;		// view twist angle, code 51
	BD		LensLength;
	BD		front_clip;		// Front clip plane Z value, code 43
	BD		back_clip;		// Back clip plane Z value, code 44
	byte	ViewMode;		// 4 bits

	RenderMode	render_mode;
	UserCoords	ucs;
		
	H		BackgroundH;
	H		VisualStyleH;
	H		SunH;
	H		BaseUCSH;
	H		NamedUCSH;
	H		LiveSectionH;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.read(height, width, center, view_target, view_dir, twist_angle, LensLength, front_clip, back_clip);
		ViewMode = (byte)bits.get_bits(4);

		if (bits.ver(VER.R2000) >= 0) 
			render_mode.read(bits);

		flags		|= bits.with_flag(pspace);

		if (bits.ver(VER.R2000) >= 0 &&  bits.get_bit())
			ucs.read(bits);

		if (bits.ver(VER.R2007) >= 0)
			flags	|= bits.with_flag(plottable);
		
		parse_handles(bits);
		H.get(bits);//XRefH

		if (bits.ver(VER.R2007) >= 0)
			bits.read(BackgroundH, VisualStyleH, SunH);

		if (bits.ver(VER.R2000) >= 0)
			bits.read(BaseUCSH, NamedUCSH);

		if (bits.ver(VER.R2007) >= 0)
			bits.read(LiveSectionH);
		return true;
	}
};


class DRW_UCS extends NamedObject {
	UserCoords ucs;
	BS		ortho_type;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		ucs.read(bits);

		if (bits.ver(VER.R2000) >= 0)
			bits.read(ortho_type);

		return parse_handles(bits);
	}
};

class DRW_VPORT extends NamedObject {
	static final int grid				= 1 << 0;
	static final int ucs_icon0			= 1 << 1;
	static final int ucs_icon1			= 1 << 2;
	static final int fastZoom			= 1 << 3;
	static final int snap_style			= 1 << 4;
	static final int snap_mode			= 1 << 5;
	static final int ucs_per_viewport	= 1 << 6;
	static final int ucs_at_origin		= 1 << 7;

	//enum VIEWMODE {
	static final int	UCSFOLLOW	= 1 << 3;
	//};
	RD2		lower_left;
	RD2		upper_right;
	RD2		center;			// center point in WCS, code 12 & 22
	RD2		snap_base;		// snap base point in DCS, code 13 & 23
	RD2		snap_spacing;	// snap Spacing, code 14 & 24
	RD2		grid_spacing;	// grid Spacing, code 15 & 25
	BD3		view_dir;		// view direction from target point, code 16, 26 & 36
	BD3		view_target;		// view target point, code 17, 27 & 37
	BD		height;			// view height, code 40
	BD		ratio;			// viewport aspect ratio, code 41
	BD		lensHeight;		// lens height, code 42
	BD		front_clip;		// front clipping plane, code 43
	BD		back_clip;		// back clipping plane, code 44
	BD		snap_angle;		// snap rotation angle, code 50
	BD		twist_angle;		// view twist angle, code 51
	int		view_mode;		// view mode, code 71
	BS		circleZoom;		// circle zoom percent, code 72
	BS		snap_isopair;	// snap isopair, code 78
	BS		gridBehavior;	// grid behavior, code 60, undocummented

	BS		grid_major;
	RenderMode	render_mode;
	UserCoords	ucs;
	
	H	bkgrdH, visualStH, sunH;
	H	namedUCSH, baseUCSH;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.read(height, ratio, center, view_target, view_dir, twist_angle, lensHeight, front_clip, back_clip);
		view_mode	= (byte)bits.get_bits(4);

		if (bits.ver(VER.R2000) >= 0) 
			render_mode.read(bits);

		bits.read(lower_left, upper_right);
		view_mode	|= bits.with_flag(UCSFOLLOW);
		bits.read(circleZoom);
		flags		|= bits.get_bits(4);
		bits.read(grid_spacing);
		flags		|= bits.get_bits(2) * snap_style;
		bits.read(snap_isopair, snap_angle, snap_base, snap_spacing);
		if (bits.ver(VER.R2000) >= 0) { 
			flags	|= bits.get_bits(2) * ucs_per_viewport;
			ucs.read(bits);
		}
		if (bits.ver(VER.R2007) >= 0)
			bits.read(gridBehavior, grid_major);

		parse_handles(bits);
		H.get(bits);//XRefH

		if (bits.ver(VER.R2000) >= 0) { 
			if (bits.ver(VER.R2007) >= 0)
				bits.read(bkgrdH, visualStH, sunH);
			bits.read(namedUCSH, baseUCSH);
		}
		return true;
	}
};

class DRW_APPID extends NamedObject {
	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.getc();//unknown

		parse_handles(bits);
		H.get(bits);//XRefH
		return true;
	}
};

class DRW_DIMSTYLE extends NamedObject {
	DimStyle	dim;
	
	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bitsin	hbits = new bitsin(bits);
		if (!parse_handles(hbits))
			return false;

		dim.read(new bitsin3(bits, hbits));
		return true;
	}
};

class DRW_VP_ENT_HDR extends Object {
	public boolean parse(bitsin2 bits) {
		return parse_head(bits) && parse_handles(bits);
	}
};

class DRW_LAYOUT extends Object {
};

class DRW_IMAGEDEF extends Object {
	TV			name;
	BL			version;				// class version, code 90, 0=R14 version
	RD2			imageSize;				// image size in pixels U value, code 10
	RD2			pixelSize;				// default size of one pixel U value, code 11
	B			loaded;					// image is loaded flag, code 280, 0=unloaded, 1=loaded
	RC		resolution;				// resolution units, code 281, 0=no, 2=centimeters, 5=inch

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;
		bits.read(version, imageSize, name, loaded, resolution, pixelSize);

		parse_handles(bits);
		H.get(bits);//XRefH
		return true;
	}
};


class DRW_GROUP extends Object {
	static final int unnamed	= 1 << 0;
	static final int selectable	= 1 << 1;

	TV	name;
	H[]	handles;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.read(name);
		flags	|= BS.get(bits).v * unnamed;
		flags	|= BS.get(bits).v * selectable;

		handles = new H[BL.get(bits).v];

		parse_handles(bits);
		H.get(bits);//XRefH

		bits.read(handles);
		return true;
	}
};

class DRW_DICTIONARYVAR extends Object {
	TV	name;
	RC	value;
	
	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;
		bits.read(value, name);
		return parse_handles(bits);
	}
};

static class MLINESTYLE_Item implements bit_readable {
	BD	offset;		// BD Offset of this segment
	CMC	color;		// CMC Color of this segment
	BS	lineindex;	// (before R2018, index)
	H	linetype;	// (before R2018, index)
	public boolean read(bit_reader bits) {
		bit_readable.instantiate(this);
		return bits.read(offset, color) && (bits.ver(VER.R2018) >= 0 || bits.read(lineindex));
	}
};

class DRW_MLINESTYLE extends Object {
	//enum MLINEFLAGS {
	static final int fill_on					= 1 << 0;
	static final int display_miters				= 1 << 1;
	static final int start_square_end_line_cap	= 1 << 4;
	static final int start_inner_arcs_cap		= 1 << 5;
	static final int start_round_outer_arcs_cap	= 1 << 6;
	static final int end_square_line_cap		= 1 << 8;
	static final int end_inner_arcs_cap			= 1 << 9;
	static final int end_round_outer_arcs_cap	= 1 << 10;


	TV		name;
	TV		desc;
	BS		mlineflags;
	CMC		fillcolor;
	BD		angle0;
	BD		angle1;
	MLINESTYLE_Item[]	items;

	public boolean parse(bitsin2 bits) {
		parse_head(bits);
		bits.read(name, desc);
		flags |= BS.get(bits).v;
		bits.read(fillcolor, angle0, angle1);
		
		bits.read(items = new MLINESTYLE_Item[bits.getc()]);
		if (parse_handles(bits)) {
			if (bits.ver(VER.R2018) >= 0)
				for (var i : items)
					bits.read(i.linetype);
		}
		return true;
	}

};

class DRW_FIELD extends Object {
	//enum EVAL_FLAGS {
		static final int Never				= 0;
		static final int OnOpen				= 1;
		static final int OnSave				= 2;
		static final int OnPlot				= 4;
		static final int OnTransmit			= 8;
		static final int OnRegeneration		= 16;
		static final int OnDemand			= 32;
	//enum FILING_FLAGS {
		static final int None				= 0;
		static final int NoFileResult		= 1;
	//enum STATE_FLAG {
		static final int Unknown			= 0;
		static final int Initialized		= 1;
		static final int Compiled			= 2;
		static final int Modified			= 4;
		static final int Evaluated			= 8;
		static final int Cached				= 16;
	//enum EVAL_STATE_FLAGS {
		static final int NotEvaluated		= 1;
		static final int Success			= 2;
		static final int EvaluatorNotFound	= 4;
		static final int SyntaxError		= 8;
		static final int InvalidCode		= 16;
		static final int InvalidContext		= 32;
		static final int OtherError			= 64;

	TV		EvaluatorID;
	TV		FieldCode;
	TV		FormatString; // R2004-

	H[]		children;
	H[]		objects;

	BL		EvaluationFlags;
	BL		FilingFlags;
	BL		StateFlags;
	BL		EvalStatusFlags;
	BL		EvalErrorCode;
	TV		EvaluationError;
	Value	value;
	TV		ValueString;
	TV		ValueStringLength;

	Map<String, Value>	child_fields;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bits.read(EvaluatorID, FieldCode);

		children = new H[BL.get(bits).v];
		objects	= new H[BL.get(bits).v];

		if (bits.ver(VER.R2004) <= 0)
			bits.read(FormatString);

		bits.read(EvaluationFlags, FilingFlags, StateFlags, EvalStatusFlags, EvalErrorCode, EvaluationError, value, ValueString, ValueStringLength);

		for (int i = 0, n = BL.get(bits).v; i < n; i++) {
			child_fields.put(TV.get(bits).s, Value.get(bits));
		}

		parse_handles(bits);
		H.get(bits);//XRefH

		return bits.read(children) && bits.read(objects);
	}
};

class DRW_PLOTSETTINGS extends Object {
	TV			name;
	BD			marginLeft;		// Size, in millimeters, of unprintable margin on left side of paper, code 40
	BD			marginBottom;	// Size, in millimeters, of unprintable margin on bottom side of paper, code 41
	BD			marginRight;	// Size, in millimeters, of unprintable margin on right side of paper, code 42
	BD			marginTop;		// Size, in millimeters, of unprintable margin on top side of paper, code 43

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(name, marginLeft, marginBottom, marginRight, marginTop)
			&& parse_handles(bits);
	}
};

class DRW_TABLESTYLE extends Object {
	static final int supress_title	= 1 << 1;
	static final int supress_header	= 1 << 2;

	TV		name;

	// 2007-
	BS		flow_dir;
	BS		style_flags;
	BD		hmargin;
	BD		vmargin;
	RowStyle	data, title, header;

	// 2010+
	CellStyle	cellstyle;
	CellStyle[]	cellstyles;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

		bitsin	hbits	= new bitsin(bits);
		parse_handles(hbits);
		bitsin3	bits3	= new bitsin3(bits, hbits);

		if (bits.ver(VER.R2007) <= 0) {
			bits3.read(name, flow_dir, style_flags, hmargin, vmargin);
			flags |= bits.get_bits(2);
			return bits3.read(data, title, header);

		}  else {

			bits3.getc();
			bits3.read(name);
			bits3.read(new BL(), new BL(), new H());

			bits3.read(cellstyle);
			cellstyles = new CellStyle[BL.get(bits3).v];
			for (var i : cellstyles)
				bits3.read(new BL(), i);
			return true;
			//return cellstyles.read(bits3, bits3.get<BL>());

		}
	}
};


class DRW_IDBUFFER extends Object {
	RC		unknown;
	BL		num_obj_ids;

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& bits.read(unknown, num_obj_ids)
			&& parse_handles(bits);
	}
};

static class AcDbObjectContextData {
	BS		class_version;
	B		is_default;
	boolean	read(bit_reader bits) {
		return bits.read(class_version, is_default);
	}
};

static class AcDbAnnotScaleObjectContextData extends AcDbObjectContextData {
	H		scale;
};

static class AcDbTextObjectContextData {
	BS		horizontal_mode;
	BD		rotation;
	RD2		ins_pt;
	RD2		alignment_pt;
	boolean	read(bit_reader bits) {
		return bits.read(horizontal_mode, rotation, ins_pt, alignment_pt);
	}
};

static class AcDbDimensionObjectContextData {
	static final int is_def_textloc	= 1 << 0;

	static final int dimtmove 		= 1 << 1;
	static final int dimtix 		= 1 << 2;
	static final int dimatfit 		= 1 << 3;
	static final int dimosxd 		= 1 << 4;
	static final int dimtofl 		= 1 << 5;
	static final int b293			= 1 << 6;

	static final int flip_arrow1 	= 1 << 7;
	static final int flip_arrow2 	= 1 << 8;
	static final int has_arrow2		= 1 << 9;

	int		flags;
	RD2		def_pt;
	BD		text_rotation;
	H		block;
	RC	override_code;
	public boolean	read(bit_reader bits) {
		bits.read(def_pt);
		flags |= bits.with_flag(is_def_textloc);
		bits.read(text_rotation, block);
		flags |= bits.get_bits(6) * dimtmove;
		bits.read(override_code);
		flags |= bits.get_bits(3) * flip_arrow1;
		return true;
	}
};

/*
class DRW_ACDB_ANNOTSCALEOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData	scale_obj;
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& scale_obj.read(bits)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_ANGDIMOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData	scale_obj;
	AcDbDimensionObjectContextData	dim_obj;
	BD3		arc_pt;
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& scale_obj.read(bits)
			&& dim_obj.read(bits)
			&& bits.read(arc_pt)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_DMDIMOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	AcDbDimensionObjectContextData dim_obj;
	BD3		first_arc_pt;
	BD3		def_pt;
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& scale_obj.read(bits) && dim_obj.read(bits)
			&& bits.read(first_arc_pt, def_pt)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_ORDDIMOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	AcDbDimensionObjectContextData dim_obj;
	BD3		feature_location_pt;
	BD3		leader_endpt;
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& AcDbObjectContextData::read(bits) && AcDbDimensionObjectContextData::read(bits)
			&& bits.read(feature_location_pt, leader_endpt)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_RADIMOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	AcDbDimensionObjectContextData dim_obj;
	BD3		first_arc_pt;
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& AcDbObjectContextData::read(bits) && AcDbDimensionObjectContextData::read(bits)
			&& bits.read(first_arc_pt jog_point)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_RADIMLGOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	AcDbDimensionObjectContextData dim_obj;
	BD3		ovr_center;
	BD3		jog_point;
	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& AcDbObjectContextData::read(bits) && AcDbDimensionObjectContextData::read(bits)
			&& bits.read(ovr_center, jog_point)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_MLEADEROBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	// ?? ...
};

class DRW_ACDB_ALDIMOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	AcDbDimensionObjectContextData dim_obj;
	BD3		dimline_pt;

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& AcDbObjectContextData::read(bits) && AcDbDimensionObjectContextData::read(bits)
			&& bits.read(dimline_pt)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_MTEXTOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;

	BL	attachment;
	//MTEXT ;
	BD3	x_axis_dir;
	BD3	ins_pt;
	BD	rect_width;
	BD	rect_height;
	BD	extents_width;
	BD	extents_height;
	BL	column_type;

	boolean parse(bitsin2 bits) {
		if (FIELD_VALUE (column_type))
		{
			FIELD_BL (num_column_heights, 72);
			FIELD_BD (column_width, 44);
			FIELD_BD (gutter, 45);
			FIELD_B (auto_height, 73);
			FIELD_B (flow_reversed, 74);
			if (!FIELD_VALUE (auto_height) && FIELD_VALUE (column_type) == 2)
				FIELD_VECTOR (column_heights, BD, num_column_heights, 46);
		}
	}
};
class DRW_ACDB_TEXTOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	AcDbDimensionObjectContextData dim_obj;

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& AcDbObjectContextData::read(bits) && AcDbTextObjectContextData::read(bits)
			&& parse_handles(bits) && bits.read(scale);
	}
};

class DRW_ACDB_LEADEROBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	RD3[] 	points;
	RD3		x_direction;
	B		b290;
	RD3		inspt_offset;
	RD3		endptproj;

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& AcDbObjectContextData::read(bits)/;.
			&& points.read(bits, bit.get<BL>())
			&& bits.read(x_direction, b290, inspt_offset, endptproj)
			&& parse_handles(bits) && bits.read(scale);
	}

};

// TOLERANCE
class DRW_ACDB_FCFOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	BD3		location;
	BD3		horiz_dir;

	boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& AcDbObjectContextData::read(bits)
			&& bits.read(location, horiz_dir)
			&& parse_handles(bits) && bits.read(scale);
	}
};
*/

class DRW_ACDB_MTEXTATTRIBUTEOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	AcDbTextObjectContextData text_obj;

	public boolean parse(bitsin2 bits) {
		if (!parse_head(bits))
			return false;

			scale_obj.read(bits);
			text_obj.read(bits);

		if (bits.get_bit()) {
			//enable_context
			//dwg_add_object (dwg);
			//context = &dwg->object[dwg->num_objects - 1];
			//dwg_setup_SCALE (_obj->context);
			//CALL_ENTITY (SCALE, _obj->context);
		}
		return parse_handles((bitsin)bits) && bits.read(scale_obj.scale);
	}
};

class DRW_ACDB_BLKREFOBJECTCONTEXTDATA_CLASS extends Object {
	AcDbAnnotScaleObjectContextData scale_obj;
	BD 		rotation;
	BD3		ins_pt;
	BD3		scale_factor;

	public boolean parse(bitsin2 bits) {
		return parse_head(bits)
			&& scale_obj.read(bits)
			&& bits.read(rotation, ins_pt, scale_factor)
			&& parse_handles((bitsin)bits) && bits.read(scale_obj.scale);
	}
};

class DRW_LONG_TRANSACTION	extends Object {};
class DRW_ACDBPLACEHOLDER	extends Object {};
class DRW_VBA_PROJECT		extends Object {};
class DRW_ACAD_PROXY_OBJECT	extends Object {};
class DRW_XRECORD			extends Object {};

class DRW_ACAD_TABLE			extends Object {};
class DRW_CELLSTYLEMAP		extends Object {};
class DRW_DBCOLOR			extends Object {};
class DRW_IMAGEDEFREACTOR	extends Object {};
class DRW_LAYER_INDEX		extends Object {};
class DRW_LWPLINE			extends Object {};
class DRW_MATERIAL			extends Object {};
class DRW_MLEADERSTYLE		extends Object {};
class DRW_PLACEHOLDER		extends Object {};
class DRW_RASTERVARIABLES	extends Object {};
class DRW_SCALE				extends Object {};
class DRW_SORTENTSTABLE		extends Object {};
class DRW_SPATIAL_FILTER		extends Object {};
class DRW_SPATIAL_INDEX		extends Object {};
class DRW_TABLEGEOMETRY		extends Object {};
class DRW_TABLESTYLES		extends Object {};
class DRW_VISUALSTYLE		extends Object {};
class DRW_WIPEOUTVARIABLE	extends Object {};
class DRW_EXACXREFPANELOBJECT extends Object {};
class DRW_NPOCOLLECTION		extends Object {};
class DRW_ACDBSECTIONVIEWSTYLE extends Object {};
class DRW_ACDBDETAILVIEWSTYLE extends Object {};

//-----------------------------------------------------------------------------
// Main class
//-----------------------------------------------------------------------------

static class MoveableClass implements bit_readable {
	static final int erase_allowed					= 1 << 0;
	static final int transform_allowed				= 1 << 1;
	static final int color_change_allowed			= 1 << 2;
	static final int layer_change_allowed			= 1 << 3;
	static final int line_type_change_allowed		= 1 << 4;
	static final int line_type_scale_change_allowed	= 1 << 5;
	static final int visibility_change_allowed		= 1 << 6;
	static final int cloning_allowed				= 1 << 7;
	static final int lineweight_change_allowed		= 1 << 8;
	static final int plot_Style_Name_change_allowed	= 1 << 9;
	static final int disable_proxy_warning_dialog	= 1 << 10;
	static final int is_R13_format_proxy			= 1 << 15;
	static final int wasazombie						= 1 << 16;
	static final int makes_entities					= 1 << 17;
	OBJECTTYPE	type;
	int			flags;
	TV			appName;
	TV			cName;
	TV			dxfName;
	int			count;
	BS			version, maintenance;

	public boolean read(bit_reader bits) {
		bit_readable.instantiate(this);
		flags		= BS.get(bits).v;
		bits.read(appName);
		bits.read(cName);
		bits.read(dxfName);
		flags		|= bits.with_flag(wasazombie);
		flags		|= BS.get(bits).v == OBJECTTYPE.ACAD_PROXY_ENTITY.value ? makes_entities : 0;
		count		= BL.get(bits).v;

		bits.read(version, maintenance, new BL(), new BL());
		type		= OBJECTTYPE.FromName(dxfName.s);
		return true;
	}
	static MoveableClass get(bit_reader bits) {
		var	t = new MoveableClass();
		t.read(bits);
		return t;
	}
};

static class ObjectHandle implements Comparable<Integer> {
	int		h;
	int		loc;
	SoftReference<Object>	obj;
	ObjectHandle(int _h, int _loc) { h = _h; loc = _loc; }

	public int compareTo(Integer o) {
		return h < o ? -1 : h > o ? 1 : 0;
	}
};

class Table<T extends Object> implements Iterable<T> {
	ObjControl	control;

	class It implements Iterator<T> {
		int		i = 0;
		public boolean	hasNext() 	{ return i < control.handles.length; }
		public T		next()		{ return (T)get_object(control.handles[i++]); }
	};

	public It	iterator() { return new It(); }

	boolean read(reader file, H ctrl, OBJECTTYPE ctype) {
		control = (ObjControl)get_object(ctrl.offset());
		return control != null && control.type == ctype;
	}
};

// DWG fields
reader		object_file;

Map<OBJECTTYPE, MoveableClass> 	classes;
ObjectHandle[] 	handles;

short					code_page;
VER						version;
byte					maintenanceVersion;
String					comments;
String					name;
HeaderVars				vars;

Table<DRW_BLOCK_HEADER>	blocks;
Table<DRW_LAYER>		layers;
Table<DRW_STYLE>		textstyles;
Table<DRW_LTYPE>		linetypes;
Table<DRW_VIEW>			views;
Table<DRW_UCS>			ucss;
Table<DRW_VPORT>		vports;
Table<DRW_APPID>		appids;
Table<DRW_DIMSTYLE>		dimstyles;
Table<DRW_VP_ENT_HDR>	vpEntHeaders;
Table<DRW_GROUP>		groups;
Table<DRW_MLINESTYLE>	mlinestyles;
Table<DRW_LAYOUT>		layouts;
Table<DRW_PLOTSETTINGS>	plotsettings;

Object get_object(int handle) {
	var	i	= Arrays.binarySearch(handles, handle);
	if (i < 0)
		return null;

	var	mit = handles[i];

	Object	obj = mit.obj != null ? mit.obj.get() : null;
	if (obj == null) {

		object_file.seek(mit.loc);

		int size = object_file.get16();
		if ((size & 0x8000) != 0)
			size += (object_file.get16() << 15) - 0x8000;

		int bsize = version.compareTo(VER.R2010) >= 0 ? size * 8 - (int) MC.get(object_file).v : size * 8;
		var offset = (int) (object_file.tell() - mit.loc);

		object_file.seek(mit.loc);
		var data = object_file.readbuff(size + offset + 2);

		var crc16 = new CRC16(0xc0c1);
		crc16.update(data);
		if (crc16.getValue() != 0)
			return null;

		var bits = new bitsin(Arrays.copyOfRange(data, offset, data.length - 2), version);
		bits.size 	= bsize;
		int soffset	= get_string_offset(bits, bsize);

		bitsin2			bits2	= soffset != 0 ? new bitsin2(bits, new bitsin(bits), soffset) : new bitsin2(bits);
		OBJECTTYPE		type 	= OBJECTTYPE.get(bits2);
		bits2.seek_bit(0);

		if (type.compareTo(OBJECTTYPE._LOOKUP) >= 0) {
			var it = classes.get(type);
			if (it == null)
				return null;

			var t = it.type;
			type = t;
		}

		obj 	= type.newInstance(DWG.this);
		if (obj != null) {
			if (!obj.parse(bits2))
				obj = null;
		}

		mit.obj = new SoftReference<Object>(obj);
	}
	return obj;
}

boolean read_header(bitsin3 bits) {
	vars	= new HeaderVars(bits);
	return true;
}

boolean read_classes(bitsin2 bits, int size) {
	classes = new HashMap<OBJECTTYPE, MoveableClass>();

	while (bits.tell_bit() < size)
		classes.put(OBJECTTYPE.FromInt(BS.get(bits).v), MoveableClass.get(bits));
	return true;
}

boolean read_handles(reader file) {
	var	handles1 = new ArrayList<ObjectHandle>();
	while (!file.eof()) {
		int size = (file.getc() << 8) | file.getc();

		file.seek_cur(-2);
		var	temp 	= file.readbuff(size);
		var	mr2 	= new MemoryReader(temp);
		mr2.seek(2);

		int handle	= 0;
		int loc		= 0;

		while (!mr2.eof()) {
			handle	+= MC.get(mr2).v;
			loc		+= MCS.get(mr2).v;
			handles1.add(new ObjectHandle(handle, loc));
		}

		//verify crc
		int crc_read = (file.getc() << 8) | file.getc();
		var	crc16 = new CRC16(0xc0c1);
		crc16.update(temp);
		if (crc16.getValue() != crc_read)
			return false;
	}
	handles = new ObjectHandle[handles1.size()];
	handles1.toArray(handles);
	return true;
}

boolean read_tables(reader file) {
	object_file = file;

	boolean ret = true;

	blocks			= new Table<>();
	layers			= new Table<>();
	textstyles		= new Table<>();
	linetypes		= new Table<>();
	views			= new Table<>();
	ucss			= new Table<>();
	vports			= new Table<>();
	appids			= new Table<>();
	dimstyles		= new Table<>();
	groups			= new Table<>();
	mlinestyles		= new Table<>();
	layouts			= new Table<>();
	plotsettings	= new Table<>();

	ret &= blocks.read    (file, vars.BLOCK_CONTROL, OBJECTTYPE.BLOCK_CONTROL_OBJ);
	ret &= layers.read    (file, vars.LAYER_CONTROL, OBJECTTYPE.LAYER_CONTROL_OBJ);
	ret &= textstyles.read(file, vars.TEXTSTYLE_CONTROL, OBJECTTYPE.STYLE_CONTROL_OBJ);
	ret &= linetypes.read (file, vars.LINETYPE_CONTROL, OBJECTTYPE.LTYPE_CONTROL_OBJ);
	ret &= views.read     (file, vars.VIEW_CONTROL, OBJECTTYPE.VIEW_CONTROL_OBJ);
	ret &= ucss.read      (file, vars.UCS_CONTROL, OBJECTTYPE.UCS_CONTROL_OBJ);
	ret &= vports.read    (file, vars.VPORT_CONTROL, OBJECTTYPE.VPORT_CONTROL_OBJ);
	ret &= appids.read    (file, vars.APPID_CONTROL, OBJECTTYPE.APPID_CONTROL_OBJ);
	ret &= dimstyles.read (file, vars.DIMSTYLE_CONTROL, OBJECTTYPE.DIMSTYLE_CONTROL_OBJ);

	if (version.compareTo(VER.R2000) <= 0) {
		vpEntHeaders	= new Table<>();
		ret &= vpEntHeaders.read(file, vars.VP_ENT_HDR_CONTROL, OBJECTTYPE.VP_ENT_HDR_CTRL_OBJ);
	}

	ret &= groups.read(file, vars.GROUP_CONTROL, OBJECTTYPE.DICTIONARY);
	ret &= mlinestyles.read(file, vars.MLINESTYLE_CONTROL, OBJECTTYPE.DICTIONARY);
	ret &= layouts.read(file, vars.LAYOUTS_CONTROL, OBJECTTYPE.DICTIONARY);
	ret &= plotsettings.read(file, vars.PLOTSETTINGS_CONTROL, OBJECTTYPE.DICTIONARY);

	return ret;
}

//-----------------------------------------------------------------------------
// readers
//-----------------------------------------------------------------------------

static class HeaderBase {
	byte[]	data;
	//char	version[11];
	//byte	maint_ver, one;
	//int	image_seeker;	//0x0d
	//byte	app_ver;		//0x11
	//byte	app_maint_ver;	//0x12
	//short	codepage;		//0x13

	VER	version()	{
		int	v = 0;
		for (int i = 2; i < 11 && data[i] >= '0' && data[i] <= '9'; i++)
			v = v * 10 + data[i] - '0';
		return VER.FromInt(v);
	}

	HeaderBase(HeaderBase b) {
		 data = b.data;
	}
	HeaderBase(reader file) {
		data = file.readbuff(128);
	}

	final VER	valid() {
		return data[0] == 'A' && data[1] == 'C' ? version() : VER.BAD;
	}
};

static final byte[] fileheader_sentinel		= {(byte)0x95, (byte)0xA0, (byte)0x4E, (byte)0x28, (byte)0x99, (byte)0x82, (byte)0x1A, (byte)0xE5, (byte)0x5E, (byte)0x41, (byte)0xE0, (byte)0x5F, (byte)0x9D, (byte)0x3A, (byte)0x4D, (byte)0x00};

static final byte[] header_sentinel			= {(byte)0xCF, (byte)0x7B, (byte)0x1F, (byte)0x23, (byte)0xFD, (byte)0xDE, (byte)0x38, (byte)0xA9, (byte)0x5F, (byte)0x7C, (byte)0x68, (byte)0xB8, (byte)0x4E, (byte)0x6D, (byte)0x33, (byte)0x5F};
static final byte[] header_sentinel_end		= {(byte)0x30, (byte)0x84, (byte)0xE0, (byte)0xDC, (byte)0x02, (byte)0x21, (byte)0xC7, (byte)0x56, (byte)0xA0, (byte)0x83, (byte)0x97, (byte)0x47, (byte)0xB1, (byte)0x92, (byte)0xCC, (byte)0xA0};

static final byte[] classes_sentinel		= {(byte)0x8D, (byte)0xA1, (byte)0xC4, (byte)0xB8, (byte)0xC4, (byte)0xA9, (byte)0xF8, (byte)0xC5, (byte)0xC0, (byte)0xDC, (byte)0xF4, (byte)0x5F, (byte)0xE7, (byte)0xCF, (byte)0xB6, (byte)0x8A};
static final byte[] classes_sentinel_end	= {(byte)0x72, (byte)0x5E, (byte)0x3B, (byte)0x47, (byte)0x3B, (byte)0x56, (byte)0x07, (byte)0x3A, (byte)0x3F, (byte)0x23, (byte)0x0B, (byte)0xA0, (byte)0x18, (byte)0x30, (byte)0x49, (byte)0x75};

static boolean check_sentinel(byte[] data, final byte[] sentinel) {
	int	p = 0;
	for (var i : sentinel) {
		if (data[p++] != i)
			return false;
	}
	return true;
}

//-----------------------------------------------------------------------------
// R12
//-----------------------------------------------------------------------------

boolean read12(reader file, HeaderBase h0) {
	final int HEADER	= 0;
	final int CLASSES	= 1;
	final int HANDLES	= 2;
	final int UNKNOWNS	= 3;
	final int TEMPLATE	= 4;
	final int AUXHEADER	= 5;

	class Section {
		int		id;
		int		address;
		int		size;
		Section(reader r) {
			id 		= r.getc();
			address = r.get32();
			size	= r.get32();
		}
		byte[] data() {
			file.seek(address);
			return file.readbuff(size);
		}
		reader reader() {
			return new SubReader(file, address, size);
		}
	};

	var	mr = new MemoryReader(h0.data);
	int num_section_locators = mr.at(0x15).get32();

	var	sections = new Section[num_section_locators];
	for (int i = 0; i < num_section_locators; i++)
		sections[i] = new Section(mr);

	//app_ver/app_maint_ver ->measuring unit?

	var	pos = file.tell();
	file.seek(0);
	var crc16 = new CRC16();
	crc16.update(file.readbuff((int)pos));
	int ckcrc = (int)crc16.getValue();

	switch (sections.length) {
		case 3:		ckcrc = ckcrc ^ 0xA598; break;
		case 4:		ckcrc = ckcrc ^ 0x8101; break;
		case 5:		ckcrc = ckcrc ^ 0x3CC4; break;
		case 6:		ckcrc = ckcrc ^ 0x8461;
	}
	int		crc	= file.get16();
	boolean	ret	= crc == ckcrc && check_sentinel(file.readbuff(16), fileheader_sentinel);
	
	{//header
		var	si = sections[HEADER];
		file.seek(si.address);

		var		data 	= file.readbuff(si.size);
		ret &= check_sentinel(data, header_sentinel);

		var		bits 	= new bitsin(data, version);
		ret &= read_header(new bitsin3(new bitsin2(bits)));
	}
		
	{// classes
		var		data 	=  sections[CLASSES].data();
		ret &= check_sentinel(data, classes_sentinel);

		int		size	= MemoryReader.wrap(data).at(16).get32();
		var		bits 	= new bitsin(data, version);
		ret &= read_classes(new bitsin2(bits), (size - 1) * 8);
	}

	// handles
	ret &= read_handles(sections[HANDLES].reader());

	return ret & read_tables(file);
}

//-----------------------------------------------------------------------------
// R18
//-----------------------------------------------------------------------------

static class decompress18 extends decompress_dwg {
	decompress18(final byte[] _compressedBuffer, byte[] _decompBuffer) {
		super(_compressedBuffer, _decompBuffer);
	}

	boolean process() {
		int litCount	= 0;

		while (buffersGood()) {
			if (litCount == 0) {
				int b = compressedByte();
				if (b > 0x0F) {
					// no literal length, this byte is next opCode
					--compPos;

				} else {
					if (b == 0) {
						litCount = 0x0f;
						b = compressedByte();
						while (b == 0 && compGood) {
							litCount += 0xFF;
							b = compressedByte();
						}
					}

					litCount += b + 3;
				}
			}

			for (int i = 0; i < litCount && buffersGood(); ++i)
				decompSet((byte) compressedByte());

			int oc = compressedByte();
			int compBytes, compOffset;
			
			if (oc < 0x40) {
				if (oc < 0x10)
					return false;
				if (oc == 0x11)
					return true;

				if (oc == 0x10 || oc == 0x20) {
					compBytes = 0;
					int b = compressedByte();
					while (b == 0 && compGood) {
						compBytes += 0xFF;
						b = compressedByte();
					}
					compBytes += b + (oc == 0x10 ? 0x09 : 0x21);

				} else {
					compBytes = oc - (oc < 0x20 ? 0x0e : 0x1e);
				}
				
				compOffset	= oc < 0x20 ? 0x3FFF : 0;
				oc 			= compressedByte();
				compOffset 	+= (compressedByte() << 6) | (oc >> 2);
				
			} else {
				compBytes	= (oc >> 4) - 1;
				compOffset	= (compressedByte() << 2) | ((oc & 0x0C) >> 2);
			}

			copy(compOffset + 1, compBytes);

			litCount = oc & 3;
		}

		return false;
	}
}

static int checksum18(int seed, byte[] data) {
	short sum1 = (short)(seed & 0xffff);
	short sum2 = (short)(seed >> 16);
	for (int i = 0; i < data.length;) {
		for (int chunk_end = Math.min(data.length, i + 0x15b0); i < chunk_end; ++i) {
			sum1 += data[i];
			sum2 += sum1;
		}
		sum1 %= 0xFFF1;
		sum2 %= 0xFFF1;
	}
	return (sum2 << 16) | (sum1 & 0xffff);
}

boolean read18(reader file, HeaderBase h0) {
	final int	SYS_SECTION		= 0x41630e3b;
	final int	DATA_SECTION	= 0x4163043b;
	final int	MAP_SECTION		= 0x4163003b;

	class Header extends HeaderBase {
		//byte	padding[3];	//0x15
		//int	security;	//0x18
		//int	unknown;
		//int	summary;
		//int	vba_project;
		//int	_0x80;
		//byte	padding2[0x54];
		Header(HeaderBase b) { super(b); }
	};

	final byte[] MAGIC = {
		(byte)0xf8, (byte)0x46, (byte)0x6a, (byte)0x04, (byte)0x96, (byte)0x73, (byte)0x0e, (byte)0xd9,
		(byte)0x16, (byte)0x2f, (byte)0x67, (byte)0x68, (byte)0xd4, (byte)0xf7, (byte)0x4a, (byte)0x4a,
		(byte)0xd0, (byte)0x57, (byte)0x68, (byte)0x76
	};

	class SystemPage {
		int	page_type;			// SYS_SECTION or MAP_SECTION
		int	decompressed_size;
		int	compressed_size;
		int	compression_type;	//2
		int	header_checksum;

		SystemPage(reader file) {
			var	b = MemoryReader.wrap(file.readbuff(5 * 4));
			page_type			= b.get32();	// SYS_SECTION or MAP_SECTION
			decompressed_size	= b.get32();
			compressed_size		= b.get32();
			compression_type	= b.get32();	//2
			header_checksum		= b.get32();
		}

		byte[] parse(reader file) {
			//SystemPage	sys	= *this;;
			//sys.header_checksum	= 0;
			//int calcsH = checksum18(0, (byte*)&sys, sizeof(sys));

			var data = file.readbuff(compressed_size);
			//int calcsD = checksum18(calcsH, data, compressed_size);

			var	out 	= new byte[decompressed_size];
			var	comp	= new decompress18(data, out);
			if (!comp.process())
				return null;
			return out;
		}
	};

	class DataSection {
		int	page_type;			// DATA_SECTION
		int	section;
		int	compressed_size;
		int	decompressed_size;
		int	offset;				// (in the decompressed buffer)
		int	header_checksum;	// section page checksum calculated from unencoded header bytes, with the data checksum as seed
		int	data_checksum;		// section page checksum calculated from compressed data bytes, with seed 0
		int	unknown;

		DataSection(reader file, int address) {
			int x = 0x4164536b ^ address;

			page_type 			= file.get32();
			section				= file.get32() ^ x;
			compressed_size		= file.get32() ^ x;
			decompressed_size	= file.get32() ^ x;
			offset				= file.get32() ^ x;
			header_checksum		= file.get32() ^ x;
			data_checksum		= file.get32() ^ x;
			unknown				= file.get32() ^ x;
		}
	};

	class PageMap {
		class Entry2 {
			int	size;
			int	address;
			Entry2(int _size, int _address) {
				size 	= _size;
				address	= _address;
			}
		};
		Map<Integer, Entry2>	entries = new HashMap<Integer, Entry2>();

		PageMap(byte[] mem) {
			var file = new MemoryReader(mem);
			int address = 0x100;
			while (!file.eof()) {
				int		page = file.get32();
				int		size = file.get32();
				if (page < 0)
					file.seek_cur(4 * 4);	// skip free fields
				else
					entries.put(page, new Entry2(size, address));
				address += size;
			}
		}
	};

	class SectionMap {
		//class Header {
		//	int	NumDescriptions;
		//	int	_0x02;
		//	int	MaxDecompressedSize;	// max size of any page
		//	int	_0x00;
		//	int	NumDescriptions2;
		//};

		class Section {
			class Page implements Comparable<Long> {
				int		page;
				int		size;
				long	offset;		// offset in dest
				long	address;	// offset in file
				SoftReference<byte[]>	data;

				Page(reader file, PageMap page_map) {
					page 	= file.get32();
					size 	= file.get32();
					offset 	= file.get64();
					address = page_map.entries.get(page).address;
				}
				boolean holds(long p) {
					return p >= offset && p < offset + page_size;
				}

				byte[]	get_data(reader file) {
					if (data != null) {
						var	d = data.get();
						if (d != null)
							return d;
					}

					file.seek(address);

					var h 			= new DataSection(file, (int)address);
					var comp_data 	= file.readbuff(h.compressed_size);

					// calculate checksum
					// int calcsD = checksum18(0, data, h.compressed_size);
					h.header_checksum = 0;
					// int calcsH = checksum18(calcsD, (byte*)&h, sizeof(h));

					var decomp_data = new byte[page_size];

					var comp = new decompress18(comp_data, decomp_data);
					if (!comp.process())
						return null;

					data = new SoftReference<>(decomp_data);
					return decomp_data;
				}
				public int compareTo(Long o) {
					return offset + page_size <= o ? -1 : 1;
				}
			};

			int		page_size;
			long	size;
			Page[]	pages;
			String 	name;

			Section(reader file, PageMap page_map) {
				size 				= file.get64();
				var	PageCount		= file.get32();
				page_size			= file.get32();	// size of a section page of this type (normally 0x7400)
				var	unknown			= file.get32();
				var	compression_type= file.get32();	// 1 = no, 2 = yes, normally 2
				var	section_id		= file.get32();
				var	encrypted		= file.get32();	// 0 = no, 1 = yes, 2 = unknown

				var	name_buff 		= file.readbuff(64);
				int	name_len		= 0;
				while (name_buff[name_len] != 0)
					++name_len;
				
				name 	= new String(name_buff, 0, name_len, StandardCharsets.UTF_8);
				pages 	= new Page[PageCount];
				for (int i = 0; i < PageCount; ++i)
					pages[i] = new Page(file, page_map);
			}

			byte[] parse(reader file) {
				var	out = new byte[(int)size];

				for (var i : pages) {
					var page_out = i.get_data(file);

					for (int j = 0, n = Math.min((int)size - (int)i.offset, page_size); j < n; j++)
						out[(int)i.offset + j] = page_out[j];
				}
				return out;
			}
			
			class Reader implements reader {
				reader	file;
				long 	p			= 0;
				long	page_offset	= 0;
				byte[]	page_data;

				boolean check_data() {
					if (page_data == null || p < page_offset || p - page_offset >= page_data.length) {
						page_data = null;
						var	i = Arrays.binarySearch(pages, p);
						var page = pages[-i - 1];
						if (!page.holds(p))
							return false;
						page_offset = page.offset;
						page_data 	= page.get_data(file);
						return true;
					}
					return true;
				}

				public long remaining() 		{ return size - p; }
				public long tell() 				{ return p; }
				public void seek(long offset) 	{ p = offset; }

				public int getc() {
					if (p >= size)
						return -1;
					return check_data() ? page_data[(int)(p++ - page_offset)] & 0xff : 0;
				}

				public int readbuff(byte[] bytes, int offset, int count) {
					int i 	= offset;
					for (int end = offset + count; i < end && check_data();) {
						int	o	= (int)(p - page_offset);
						int	n1 	= Math.min(end - i, page_data.length - o);
						p += n1;
						while (n1-- > 0)
							bytes[i++] = page_data[o++];
					}
					return i - offset;
				}

				Reader(reader _file) {
					file = _file;
				}
			}
		};
		Map<String, Section>	sections = new HashMap<String, Section>();

		SectionMap(byte[] mem, PageMap page_map) {
			var	file = new MemoryReader(mem);
			int	n = file.get32();
			file.seek(5 * 4);
			
			for (int i = 0; i < n; i++) {
				var s = new Section(file, page_map);
				sections.put(s.name, s);
			}
		}

		byte[]	data(reader file, String name) {
			var si = sections.get(name);
			return si.parse(file);
		}

		reader reader(reader file, String name) {
			var si = sections.get(name);
			return si.new Reader(file);
		}
	};

	class FileHeader {
		byte[]	data;
		//char	id[12];					//0x00 12	“AcFssFcAJMB” file ID string
		//int	_0;						//0x0C 4	0x00 (long)
		//int	_6c;					//0x10 4	0x6c (long)
		//int	_4;						//0x14 4	0x04 (long)
		//int	root_gap;				//0x18 4	Root tree node gap
		//int	lower_left_gap;			//0x1C 4	Lowermost left tree node gap
		//int	lower_right_gap;		//0x20 4	Lowermost right tree node gap
		//int	_1;						//0x24 4	Unknown long (ODA writes 1)
		//int	last_section_page;		//0x28 4	Last section page Id
		//long	last_section_page_end;	//0x2C 8	Last section page end address
		//long	second_header;			//0x34 8	Second header data address pointing to the repeated header data at the end of the file
		//int	gap;					//0x3C 4	Gap amount
		//int	section_page;			//0x40 4	Section page amount
		//int	_20;					//0x44 4	0x20 (long)
		//int	_80;					//0x48 4	0x80 (long)
		//int	_40;					//0x4C 4	0x40 (long)
		//int	section_page_map;		//0x50 4	Section Page Map Id
		//long	section_page_map_addr;	//0x54 8	Section Page Map address (add 0x100 to this value)
		//int	section_map;			//0x5C 4	Section Map Id
		//int	section_page_array;		//0x60 4	Section page array size
		//int	gap_array;				//0x64 4	Gap array size
		//int	crc32;					//0x68 4	CRC32 (long) CRC calculation is done including the 4 CRC bytes that are initially zero

		long	section_page_map_addr() { return MemoryReader.wrap(data).at(0x54).get64(); }
		int		section_map() 			{ return MemoryReader.wrap(data).at(0x5c).get32(); }

		FileHeader(reader file) {
			data = file.readbuff(0x6c);
			int		seed = 1;
			for (int i = 0; i < 0x6c; i++) {
				seed = (seed * 0x343fd) + 0x269ec3;
				data[i] ^= seed >> 16;
			}

		}
	};

	Header	h = new Header(h0);

	file.seek(0x80);
	FileHeader	fh = new FileHeader(file);

	var	crc = new CRC32();
	crc.update(fh.data);

	if (Arrays.compare(file.readbuff(20), MAGIC) != 0)
		return false;

	file.seek(fh.section_page_map_addr() + 0x100);

	var	page = new SystemPage(file);
	if (page.page_type != SYS_SECTION)
		return false;

	var	data 		= page.parse(file);
	var	page_map 	= new PageMap(data);
	var sectionMap 	= page_map.entries.get(fh.section_map());
	file.seek(sectionMap.address);

	page = new SystemPage(file);
	if (page.page_type != MAP_SECTION)
		return false;

	data = page.parse(file);
	var	sections = new SectionMap(data, page_map);
	
	boolean	ret = true;
	//read_header
	{
		data	= sections.data(file, "AcDb:Header");
		int	size = MemoryReader.wrap(data).at(16).get32();

		ret		&= check_sentinel(data, header_sentinel);
		ret		&= check_sentinel(Arrays.copyOfRange(data, 16 + 4 + 4 + 2 + size, data.length), header_sentinel_end);

		var	crc16 = new CRC16(0xc0c1);
		crc16.update(data, 16, size + 10);
		ret		&=  crc16.getValue() == 0;

		bitsin	bits	= new bitsin(Arrays.copyOfRange(data, 16 + 4 + 4, data.length), version);
		int		bitsize = bits.get32();

		bitsin	hbits	= new bitsin(bits);
		hbits.seek_bit(bitsize);

		var soffset = get_string_offset(bits, bitsize);
		if (soffset != 0) {
			bitsin	sbits = new bitsin(bits);
			ret &= read_header(new bitsin3(new bitsin2(bits, sbits, soffset), hbits));
		} else {
			ret &= read_header(new bitsin3(new bitsin2(bits), hbits));
		}
	}

	//read_classes
	{
		data	= sections.data(file, "AcDb:Classes");
		int	size = MemoryReader.wrap(data).at(16).get32();

		ret		&= check_sentinel(data, classes_sentinel);
		ret		&= check_sentinel(Arrays.copyOfRange(data, 16 + 4 + 4 + 2 + size, data.length), classes_sentinel_end);

		var	crc16 = new CRC16(0xc0c1);
		crc16.update(data, 16, size + 10);
		ret		&=  crc16.getValue() == 0;

		bitsin	bits	= new bitsin(Arrays.copyOfRange(data, 16 + 4 + 4, data.length), version);
		int		bitsize = bits.get32();

		int		maxClassNum = BS.get(bits).v;
		int		Rc1	= bits.getc();
		int		Rc2	= bits.getc();
		boolean	Bit	= bits.get_bit();

		var soffset = get_string_offset(bits, bitsize);
		if (soffset != 0) {
			bitsin	sbits = new bitsin(bits);
			ret &= read_classes(new bitsin2(bits, sbits, soffset), soffset);
		} else {
			ret &= read_classes(new bitsin2(bits), bitsize);
		}
	}

	//read_handles
	ret &= read_handles(sections.reader(file, "AcDb:Handles"));

	//read_tables
	return ret & read_tables(sections.reader(file, "AcDb:AcDbObjects"));
}

//-----------------------------------------------------------------------------
// R21
//-----------------------------------------------------------------------------

static class decompress21 extends decompress_dwg {
	final static int 	MaxBlockLength = 32;
	static final byte[][] CopyOrder = {
		null,
		{0},
		{1,0},
		{2,1,0},
		{0,1,2,3},
		{4,0,1,2,3},
		{5,1,2,3,4,0},
		{6,5,1,2,3,4,0},
		{0,1,2,3,4,5,6,7},
		{8,0,1,2,3,4,5,6,7},
		{9,1,2,3,4,5,6,7,8,0},
		{10,9,1,2,3,4,5,6,7,8,0},
		{8,9,10,11,0,1,2,3,4,5,6,7},
		{12,8,9,10,11,0,1,2,3,4,5,6,7},
		{13,9,10,11,12,1,2,3,4,5,6,7,8,0},
		{14,13,9,10,11,12,1,2,3,4,5,6,7,8,0},
		{8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{9,10,11,12,13,14,15,16,8,0,1,2,3,4,5,6,7},
		{17,9,10,11,12,13,14,15,16,1,2,3,4,5,6,7,8,0},
		{18,17,16,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{16,17,18,19,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{20,16,17,18,19,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{21,20,16,17,18,19,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{22,21,20,16,17,18,19,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{16,17,18,19,20,21,22,23,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{17,18,19,20,21,22,23,24,16,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{25,17,18,19,20,21,22,23,24,16,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{26,25,17,18,19,20,21,22,23,24,16,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{24,25,26,27,16,17,18,19,20,21,22,23,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{28,24,25,26,27,16,17,18,19,20,21,22,23,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{29,28,24,25,26,27,16,17,18,19,20,21,22,23,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
		{30,26,27,28,29,18,19,20,21,22,23,24,25,10,11,12,13,14,15,16,17,2,3,4,5,6,7,8,9,1,0},
		{24,25,26,27,28,29,30,31,16,17,18,19,20,21,22,23,8,9,10,11,12,13,14,15,0,1,2,3,4,5,6,7},
	};

	decompress21(final byte[] _compressedBuffer, byte[] _decompBuffer) {
		super(_compressedBuffer, _decompBuffer);
	}

	boolean process() {
		int	length		= 0;
		int	opCode		= compressedByte();

		if ((opCode >> 4) == 2) {
			compPos	+= 2;
			length		= compressedByte() & 0x07;
		}

		while (buffersGood()) {
			if (length == 0) {
				//litlength
				length = 8 + opCode;
				if (length == 0x17) {
					int n = compressedByte();
					length += n;
					if (n == 0xff) {
						do {
							n = compressedByte() | (compressedByte() << 8);
							length += n;
						} while (n == 0xffff);
					}
				}
			}

			while (length != 0) {
				int	n = Math.min(length, MaxBlockLength);
				for (var i : CopyOrder[n])
					decompSet(compBuffer[compPos + i]);
				compPos	+= n;
				length	-= n;
			}

			length = 0;
			opCode = compressedByte();
			for (;;) {
				int	sourceOffset	= 0;

				switch (opCode >> 4) {
					case 0:
						length			= (opCode & 0x0f) + 0x13;
						sourceOffset	= compressedByte();
						opCode			= compressedByte();
						length			+= ((opCode >> 3) & 0x10);
						sourceOffset	+= ((opCode & 0x78) << 5) + 1;
						break;
					case 1:
						length			= (opCode & 0xf) + 3;
						sourceOffset	= compressedByte();
						opCode			= compressedByte();
						sourceOffset	+= ((opCode & 0xf8) << 5) + 1;
						break;
					case 2:
						sourceOffset	= compressedByte() | (compressedByte() << 8);
						length			= opCode & 7;
						if ((opCode & 8) == 0) {
							opCode		= compressedByte();
							length		+= opCode & 0xf8;
						} else {
							++sourceOffset;
							length		+= compressedByte() << 3;
							opCode		= compressedByte();
							length		+= ((opCode & 0xf8) << 8) + 0x100;
						}
						break;
					default:
						length			= opCode >> 4;
						sourceOffset	= opCode & 15;
						opCode			= compressedByte();
						sourceOffset	+= ((opCode & 0xf8) << 1) + 1;
						break;
				}

				if (!copy(sourceOffset, length))
					return false;

				length = opCode & 7;
				if (length != 0)
					break;

				opCode = compressedByte();
				if ((opCode >> 4) == 0)
					break;

				if ((opCode >> 4) == 15)
					opCode &= 15;
			}
		}
		return buffersGood();
	}
}

byte[] parseSysPage21(reader file, long sizeCompressed, long sizeUncompressed, long correctionFactor, long offset) {
	int chunks = (int)((((sizeCompressed + 7) /8 * 8) * correctionFactor + 239 - 1) / 239);
	int fpsize = chunks * 255;

	file.seek(offset);

	var	data = file.readbuff((int)fpsize);

	var	data_rs = new byte[fpsize];
//		decodeI<239,0x96,8,8>(data, data_rs,chunks);

	var	out		= new byte[(int)sizeUncompressed];
	var	comp 	= new decompress21(data_rs, out);
	if (!comp.process())
		return null;
	return out;
};

boolean read21(reader file, HeaderBase h0) {
	//class Header extends HeaderBase {
	//	byte	padding[3];	//0x15
	//	int		security;	//0x18
	//	int		unknown;
	//	int		summary;
	//	int		vba_project;
	//	int		_0x80;		// offset to FileHeaderHeader?
	//	int		app_info_addr;
	//	byte	padding2[0x50];
	//};

	class FileHeaderHeader {
		long	crc;
		long	unknown_key;
		long	compressed_crc;
		int		compressed_size;	//(if < 0, not compressed)
		int		uncompressed_size;
	};

	class FileHeader {
		long	header_size;// (normally 0x70)
		long	File_size;
		long	PagesMapCrcCompressed;
		long	PagesMapCorrectionFactor;
		long	PagesMapCrcSeed;
		long	PagesMap2offset;// (relative to data page map 1, add 0x480 to get stream position)
		long	PagesMap2Id;
		long	PagesMapOffset;// (relative to data page map 1, add 0x480 to get stream position)
		long	PagesMapId;
		long	Header2offset;	// (relative to page map 1 address, add 0x480 to get stream position)
		long	PagesMapSizeCompressed;
		long	PagesMapSizeUncompressed;
		long	PagesAmount;
		long	PagesMaxId;
		long	Unknown1;// (normally 0x20)
		long	Unknown2;// (normally 0x40)
		long	PagesMapCrcUncompressed;
		long	Unknown3;// (normally 0xf800)
		long	Unknown4;// (normally 4)
		long	Unknown5;// (normally 1)
		long	SectionsAmount;// (number of sections + 1)
		long	SectionsMapCrcUncompressed;
		long	SectionsMapSizeCompressed;
		long	SectionsMap2Id;
		long	SectionsMapId;
		long	SectionsMapSizeUncompressed;
		long	SectionsMapCrcCompressed;
		long	SectionsMapCorrectionFactor;
		long	SectionsMapCrcSeed;
		long	StreamVersion;// (normally 0x60100)
		long	CrcSeed;
		long	CrcSeedEncoded;
		long	RandomSeed;
		long	HeaderCRC64;
	};

	class PageMap {
		class Entry {
			long	size;
			long	id;
			Entry(reader file) {
				size = file.get64();
				id	= file.get64();
			}
		};
		class Entry2 {
			int		page;
			int		size;
			long	address;
			Entry2(int _page, int _size, long _address) { page = _page; size = _size; address = _address; }
		};
		Map<Integer, Entry2>	entries = new HashMap<Integer, Entry2>();

		PageMap(byte[] mem) {
			var	mr = new MemoryReader(mem);
			long	address = 0x480;
			while (!mr.eof()) {
				var	i = new Entry(mr);
				entries.put(Math.abs((int)i.id), new Entry2((int)i.id, (int)i.size, address));
				address += i.size;
			}
		}
	};

	class SectionMap {
		class Description {
			class Page {
				long offset;
				long size;
				long id;
				long uncompressed_size;
				long compressed_size;
				long checksum;
				long crc;
			};
			long	DataSize;
			long	MaxSize;
			long	Encryption;
			long	HashCode;
			long	SectionNameLength;
			long	Unknown;
			long	Encoding;
			long	NumPages;
			String	Name;
			Page[]	pages;

			//auto	name()	const	{ return SectionNameLength ? SectionName : nullptr; }
			//auto	pages() const	{ return make_range_n((Page*)(SectionName + SectionNameLength + (SectionNameLength != 0)), NumPages); }
			//auto	next()	const	{ return (const Description*)pages().end(); }
			Description(reader file) {
			}
		};

		class Section {
			class Page {
				int		page;
				int		size;
				int		compressed_size;
				long	offset;		// offset in dest
				long	address;	// offset in file
				Page(Description.Page p, PageMap page_map) {
					page = (int)p.id;
					size = (int)p.size;
					offset = p.offset;
					address = page_map.entries.get(page).address;
				}
			};
			int		page_size;
			long	size;
			Page[]	pages;

			byte[] parse(reader file) {
				var	out 		= new byte[(int)size];

				for (var i : pages) {
					file.seek(i.address);

					var	data 	= file.readbuff((int)i.compressed_size);
					var	data_rs	= new byte[i.compressed_size];

					//decodeI<251, 0xb8, 8, 2>(data, data_rs, i.size / 255);

					var	page_out	= new byte[i.size];
					var comp 		= new decompress21(data_rs, page_out);
					if (!comp.process())
						return null;
				}
				return out;
			}
			Section(Description d, PageMap page_map) {
				page_size	= (int)d.MaxSize;
				pages 		= new Page[d.pages.length];
				for (int i = 0; i < d.pages.length; ++i)
					pages[i] = new Page(d.pages[i], page_map);
			}
		};

		Map<String, Section>	sections = new HashMap<String, Section>();

		SectionMap(byte[] mem, PageMap page_map) {
			var	file = new MemoryReader(mem);
			byte nextId = 1;
			while (!file.eof()) {
				var	d = new Description(file);
				var	s = new Section(d, page_map);
				sections.put(d.Name, s);
			}
		}

		byte[]	data(reader file, String name) {
			var si = sections.get(name);
			return si.parse(file);
		}
	};

	file.seek(0x80);//h->_0x80?

 
	var	fileHdrRaw = file.readbuff(0x2fd);
	var fileHdrdRS = new byte[0x2CD];

	//decodeI<239, 0x96, 8, 8>(fileHdrRaw, fileHdrdRS, 3);
/*
	FileHeaderHeader	*fhh	= (FileHeaderHeader*)fileHdrdRS;
	malloc_block		fh_data;

	if (fhh->compressed_size < 0) {
		fh_data.resize(-fhh->compressed_size);
		fh_data.copy_from(fhh + 1);
	} else {
		decompress21	comp(fhh + 1, fh_data, fhh->compressed_size, fhh->uncompressed_size);
		if (!comp.process())
			return false;
	}

	FileHeader	*fh	= fh_data;
	malloc_block	data;
	if (!(data = parseSysPage(fh->PagesMapSizeCompressed, fh->PagesMapSizeUncompressed, fh->PagesMapCorrectionFactor, 0x480+fh->PagesMapOffset)))
		return false;

	PageMap		page_map(data);
	auto		sectionMap = page_map.entries[fh->SectionsMapId];

	if (!(data = parseSysPage(fh->SectionsMapSizeCompressed, fh->SectionsMapSizeUncompressed, fh->SectionsMapCorrectionFactor, sectionMap.get()->address)))
		return false;

	SectionMap	sections(data, page_map);

	boolean	ret = true;
	//read_header
	{
		data	= sections.data(file, "AcDb:Header");
		if (!check_sentinel(data, header_sentinel))
			return false;

		SectionStart	*start = data;
		bitsin			bits(data.slice(sizeof(SectionStart)), version);
		int			bitsize = int.get(bits);

		bitsin			hbits(bits);
		hbits.seek_bit(bitsize);

		if (auto soffset = get_string_offset(bits, bitsize)) {
			bitsin		sbits(bits);
			return read_header(bitsin2(bits, sbits, soffset));
		}
		bitsin2	bits2(bits);
		ret &= read_header(bitsin3(bits2, hbits));
	}

	//read_classes
	{
		data	= sections.data(file, "AcDb:Classes");
		if (!check_sentinel(data, classes_sentinel))
			return false;

		SectionStart	*start = data;
		bitsin			bits(data.slice(sizeof(SectionStart)), version);
		int			bitsize = int.get(bits);

		int	maxClassNum = BS.get(bits);
		byte	Rc1	= bits.getc();
		byte	Rc2	= bits.getc();
		boolean	Bit	= bits.get_bit();

		if (auto soffset = get_string_offset(bits, bitsize)) {
			bitsin	sbits(bits);
			ret &= read_classes(bitsin2(bits, sbits, soffset), soffset);
		} else {
			ret &= read_classes(bits, bitsize);
		}
	}


	//read_handles
	ret &= read_handles(MemoryReader(malloc_block(sections.data(file, "AcDb:Handles"))));

	//read_tables
	return ret & read_tables(new MemoryReader(sections.data(file, "AcDb:AcDbObjects")));
*/
	return true;
}

boolean open(File file) {
	try {
		reader 		in		= new FileReader(new RandomAccessFile(file, "r"));
		HeaderBase	head	= new HeaderBase(in);

		switch (version = head.valid()) {
			case R13:
			case R14:
			case R2000:
				return read12(in, head);
			case R2007:
				return read21(in, head);
			case R2004:
			case R2010:
			case R2013:
			case R2018:
				return read18(in, head);
			default:
				return false;
		}

	} catch (java.io.IOException e) {
		return false;
	}
}

DWG() {}
DWG(File file) {
	open(file);
}

}
