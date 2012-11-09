package org.xmlcml.graphics.pdf2svg.raw;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Util;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/** manages a generic set of fonts
 * should not depend on prefix, bold, italic, MT or PS suffixes, etc.
 * @author pm286
 *
 // standard
    <font family="Courier" fontType="PDType1Font" note="a standard14 font" serif="yes" unicode="yes"/>
    or
  // non-standard
    <font family="FooBar" fontType="PDType1Font" standardFont="Helvetica" note="" serif="" unicode="guessed"/>

 *
 */
public class FontFamily {

	public final static Logger LOG = Logger.getLogger(FontFamily.class);
	// XML
	public static final String CODE_POINT_SET = "codePointSet";
	public final static String FONT_FAMILY = "fontFamily";
	public static final String NAME = "name";
	public static final String FONT_TYPE = "fontType";
	public static final String MONOSPACED = "monospaced";
	public static final String NOTE = "note";
	public static final String SERIF = "serif";
	public static final String STANDARD_FONT = "standardFont";
	public static final String UNICODE = "unicode";

	private String name;
	private String fontType;
	private String standardFont;
	private String unicode;
	private String serif;
	private String monospaced;
	private String note;
	private CodePointSet codePointSet;

	public FontFamily() {
		
	}

	public static FontFamily createFromElement(Element fontFamilyElement) {
		FontFamily fontFamily = null;
		try {
			fontFamily = new FontFamily();
			if (!(FONT_FAMILY.equals(fontFamilyElement.getLocalName()))) {
				throw new RuntimeException("FontFamilySet children must be: "+FONT_FAMILY);
			}
			fontFamily.name = fontFamilyElement.getAttributeValue(NAME);
			if (fontFamily.name == null) {
				throw new RuntimeException("<FontFamily> must have familyName attribute");
			}
			fontFamily.fontType = fontFamilyElement.getAttributeValue(FONT_TYPE);
			fontFamily.standardFont = fontFamilyElement.getAttributeValue(STANDARD_FONT);
			fontFamily.unicode = fontFamilyElement.getAttributeValue(UNICODE);
			fontFamily.serif = fontFamilyElement.getAttributeValue(SERIF);
			fontFamily.monospaced = fontFamilyElement.getAttributeValue(MONOSPACED);
			fontFamily.note = fontFamilyElement.getAttributeValue(NOTE);
			String codePointSetName = fontFamilyElement.getAttributeValue(CODE_POINT_SET);
			if (codePointSetName != null) {
				CodePointSet codePointSet = CodePointSet.readCodePointSet(codePointSetName);
				if (codePointSet == null) {
					throw new RuntimeException("Cannot read codePointSet: "+codePointSetName);
				}
				fontFamily.setCodePointSet(codePointSet);
				LOG.debug("CPS: "+fontFamily.getCodePointSet());
			}
		} catch (Exception e) {
			throw new RuntimeException("invalid FontFamilyElement: "+((fontFamilyElement == null) ? null : fontFamilyElement.toXML()), e);
		}
		return fontFamily;
	}

	private void setCodePointSet(CodePointSet codePointSet) {
		this.codePointSet = codePointSet;
	}

	public Element createElement() {
		Element FontFamilyElement = new Element(FONT_FAMILY);
		if (name == null) {
			throw new RuntimeException("familyName must not be null");
		}
		FontFamilyElement.addAttribute(new Attribute(NAME, ""+name));
		if (standardFont != null) {
			FontFamilyElement.addAttribute(new Attribute(STANDARD_FONT, standardFont));
		}
		if (note != null) {
			FontFamilyElement.addAttribute(new Attribute(NOTE, note));
		}
		if (unicode != null) {
			FontFamilyElement.addAttribute(new Attribute(UNICODE, unicode));
		}
		if (serif != null) {
			FontFamilyElement.addAttribute(new Attribute(SERIF, serif));
		}
		if (monospaced != null) {
			FontFamilyElement.addAttribute(new Attribute(MONOSPACED, monospaced));
		}
		if (fontType != null) {
			FontFamilyElement.addAttribute(new Attribute(FONT_TYPE, fontType));
		}
		return FontFamilyElement;
	}

	public String getUnicode() {
		return unicode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CodePointSet getCodePointSet() {
		return codePointSet;
	}

}
