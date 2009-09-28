package gov.nih.nci.evs.browser.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

import org.LexGrid.LexBIG.DataModel.Collections.NCIChangeEventList;
import org.LexGrid.LexBIG.DataModel.NCIHistory.NCIChangeEvent;
import org.LexGrid.LexBIG.DataModel.NCIHistory.types.ChangeType;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.History.HistoryService;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.concepts.Concept;

import static gov.nih.nci.evs.browser.common.Constants.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008,2009 NGIT. This software was developed in conjunction with the National Cancer Institute,
 * and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
 * "This product includes software developed by NGIT and the National Cancer Institute."
 * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself,
 * wherever such third-party acknowledgments normally appear.
 * 3. The names "The National Cancer Institute", "NCI" and "NGIT" must not be used to endorse or promote products derived from this software.
 * 4. This license does not authorize the incorporation of this software into any third party proprietary programs. This license does not authorize
 * the recipient to use any trademarks owned by either NCI or NGIT
 * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 * NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 */

public class HistoryUtils {

	private static DateFormat _dataFormatter = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static Vector<String> getTableHeader() {
		Vector<String> v = new Vector<String>();
		v.add("Edit Actions");
		v.add("Date");
		v.add("Reference Concept");
		return v;
	}


	public static boolean isHistoryServiceAvailable(String codingSchemeName) {
		LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
		HistoryService hs = null;
		try {
			hs = lbSvc.getHistoryService(codingSchemeName);
			if (hs != null) return true;
		} catch (Exception ex) {
			System.out.println("Unable to getHistoryService for " + codingSchemeName);
		}
		return false;
	}

/*
	public static Vector<String> getEditActions(String codingSchemeName,
			String vers, String ltag, String code) throws LBException {
		LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
		//HistoryService hs = lbSvc.getHistoryService(CODING_SCHEME_NAME);
		HistoryService hs = lbSvc.getHistoryService(codingSchemeName);

		try {
			NCIChangeEventList list = hs.getEditActionList(Constructors
					.createConceptReference(code, null), null, null);
			return getEditActions(codingSchemeName, vers, ltag, code, list);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

*/

	public static Vector<String> getEditActions(String codingSchemeName,
			String vers, String ltag, String code) throws LBException {
		LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
		//HistoryService hs = lbSvc.getHistoryService(CODING_SCHEME_NAME);
		HistoryService hs = lbSvc.getHistoryService(codingSchemeName);
		return getEditActions(lbSvc, hs, codingSchemeName, vers, ltag, code);
	}

    //[#23370] NCIt concept history shows wrong Code (KLO, 09/28/09)
    private static Vector<String> getEditActions(LexBIGService lbSvc, HistoryService hs, String codingSchemeName,
            String vers, String ltag, String code) throws LBException {
        try {
			Concept c = DataUtils.getConceptByCode(codingSchemeName, vers, ltag, code);
			if (c == null) return null;
			Boolean isActive = c.isIsActive();
			NCIChangeEventList list = null;
			if (isActive != null &&  isActive.equals(Boolean.FALSE)) {
				list = hs.getDescendants(Constructors.createConceptReference(code, null));
			} else {
			    list = hs.getEditActionList(Constructors.createConceptReference(code, null), null, null);
			}
			return getEditActions(codingSchemeName, vers, ltag, code, list);
	    } catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }


	private static Vector<String> getEditActions(String codingSchemeName,
			String vers, String ltag, String code, NCIChangeEventList list) {
		Enumeration<NCIChangeEvent> enumeration = list.enumerateEntry();
		Vector<String> v = new Vector<String>();
		HashSet<String> hset = new HashSet<String>();
		while (enumeration.hasMoreElements()) {
			NCIChangeEvent event = enumeration.nextElement();
			ChangeType type = event.getEditaction();
			Date date = event.getEditDate();
			String rCode = event.getReferencecode();
			String desc = "N/A";
			if (rCode != null && rCode.length() > 0
					&& !rCode.equalsIgnoreCase("null")) {
				Concept c = DataUtils.getConceptByCode(codingSchemeName, vers,
						ltag, rCode);
				//KLO
				if (c != null) {
					String name = c.getEntityDescription().getContent();
					desc = name + " (Code " + rCode + ")";
				} else {
					desc = rCode;
				}
			}

			String info = type + "|" + _dataFormatter.format(date) + "|" + desc;
			if (hset.contains(info))
				continue;
			v.add(info);
			hset.add(info);
		}
		return v;
	}

    public static Vector<String> getAncestors(String codingSchemeName,
            String vers, String ltag, String code) {
        LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
        HistoryService hs = null;
        try {
			hs = lbSvc.getHistoryService(codingSchemeName);
		} catch (Exception ex) {
			System.out.println("Unable to getHistoryService for " + codingSchemeName);
			return null;
		}

        try {
 			NCIChangeEventList list = hs.getAncestors(Constructors
                     .createConceptReference(code, null));

			return getEditActions(codingSchemeName, vers, ltag, code, list);
	    } catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }

    public static Vector<String> getDescendants(String codingSchemeName,
            String vers, String ltag, String code) {
        LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
        HistoryService hs = null;
        try {
			hs = lbSvc.getHistoryService(codingSchemeName);
		} catch (Exception ex) {
			System.out.println("Unable to getHistoryService for " + codingSchemeName);
			return null;
		}

        try {
 			NCIChangeEventList list = hs.getDescendants(Constructors
                     .createConceptReference(code, null));

			return getEditActions(codingSchemeName, vers, ltag, code, list);
	    } catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }

    public static Vector<String> getDescendantCodes(String codingSchemeName,
            String vers, String ltag, String code) {
        LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
        HistoryService hs = null;
        try {
			hs = lbSvc.getHistoryService(codingSchemeName);
		} catch (Exception ex) {
			System.out.println("Unable to getHistoryService for " + codingSchemeName);
			return null;
		}

        Vector<String> v = new Vector<String>();
        try {
 			NCIChangeEventList list = hs.getDescendants(Constructors
                     .createConceptReference(code, null));

			HashSet<String> hset = new HashSet<String>();
			Enumeration<NCIChangeEvent> enumeration = list.enumerateEntry();
			while (enumeration.hasMoreElements()) {
				NCIChangeEvent event = enumeration.nextElement();
				ChangeType type = event.getEditaction();
				Date date = event.getEditDate();
				String rCode = event.getReferencecode();
				String name = "unassigned";
				if (rCode != null && rCode.length() > 0
						&& !rCode.equalsIgnoreCase("null")) {
					Concept c = DataUtils.getConceptByCode(codingSchemeName, vers,
							ltag, rCode);

					if (c != null) {
						name = c.getEntityDescription().getContent();
					}
				}
				String info = name + "|" + rCode;
				if (hset.contains(info))
					continue;
				v.add(info);
				hset.add(info);
			}

	    } catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return v;
	}

}
