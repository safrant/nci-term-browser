package gov.nih.nci.evs.browser.utils;

import java.util.*;
import java.io.*;
import java.util.Map.Entry;
import gov.nih.nci.evs.browser.bean.*;

import org.LexGrid.concepts.*;
import org.LexGrid.LexBIG.Utility.Iterators.*;
import org.LexGrid.LexBIG.DataModel.Core.*;

import java.util.Enumeration;
import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.ActiveOption;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;
import org.LexGrid.commonTypes.EntityDescription;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.PropertyQualifier;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.concepts.Comment;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;

import org.apache.log4j.*;
import gov.nih.nci.evs.browser.common.Constants;
import org.LexGrid.codingSchemes.*;
import org.LexGrid.naming.SupportedProperty;
import org.LexGrid.LexBIG.DataModel.Collections.SortOptionList;
import org.apache.commons.lang.*;

import org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.RenderingDetail;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.descriptors.RenderingDetailDescriptor;
import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeTagList;
import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeRenderingList;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering;
import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;


public class VisUtils {
    private static Logger _logger = Logger.getLogger(VisUtils.class);
    private LexBIGService lbSvc = null;
    private LexBIGServiceConvenienceMethods lbscm = null;

    public static int NODES_ONLY = 1;
    public static int EDGES_ONLY = 2;
    public static int NODES_AND_EDGES = 3;

    public static final String[] ALL_RELATIONSHIP_TYPES = {"type_superconcept",
                                                           "type_subconcept",
                                                           "type_role",
                                                           "type_inverse_role",
                                                           "type_association",
                                                           "type_inverse_association"};

	public VisUtils() {

	}

	public VisUtils(LexBIGService lbSvc) {
        this.lbSvc = lbSvc;
        try {
            lbscm =
                (LexBIGServiceConvenienceMethods) lbSvc
                    .getGenericExtension("LexBIGServiceConvenienceMethods");
            lbscm.setLexBIGService(lbSvc);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


    public String getLabel(String name, String code) {
		name = encode(name);
		StringBuffer buf = new StringBuffer();
		buf.append(name + " (" + code + ")");
		return buf.toString();
	}

    public String getLabel(String line) {
        Vector u = gov.nih.nci.evs.browser.utils.StringUtils.parseData(line);
        String name = (String) u.elementAt(0);
        name = encode(name);
        String code = (String) u.elementAt(1);
        return getLabel(name, code);
	}

    public String getFieldValue(String line, int index) {
        Vector u = gov.nih.nci.evs.browser.utils.StringUtils.parseData(line);
        return (String) u.elementAt(index);
	}

	public String encode(String t) {
		if (t == null) return null;
		t = t.replaceAll("'", " ");
		return t;
	}


    public String generateDiGraph(String scheme, String version, String namespace, String code) {
		boolean useNamespace = false;
		if (namespace != null) useNamespace = true;
		Entity concept = new ConceptDetails(lbSvc).getConceptByCode(scheme, version, code, namespace, useNamespace);
		String name = "<NO DESCRIPTION>";
		if (concept.getEntityDescription() != null) {
			name = concept.getEntityDescription().getContent();
		}
		name = encode(name);

		if (gov.nih.nci.evs.browser.utils.StringUtils.isNullOrBlank(namespace)) {
			namespace = concept.getEntityCodeNamespace();
		}

		StringBuffer buf = new StringBuffer();
        buf.append("\ndigraph {").append("\n");
        buf.append("node [shape=oval fontsize=16]").append("\n");
        buf.append("edge [length=100, color=gray, fontcolor=black]").append("\n");

        String focused_node_label = "\"" + getLabel(name, code) + "\"" ;

        RelationshipUtils relUtils = new RelationshipUtils(lbSvc);
        HashMap relMap = relUtils.getRelationshipHashMap(scheme, version, code, namespace, useNamespace);

		String key = "type_superconcept";
		ArrayList list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label = "\"" + getLabel(t) + "\"" ; //getLabel(t);
				String rel_label = "is_a";
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_subconcept";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label = "\"" + getLabel(t) + "\"" ; //getLabel(t);
				String rel_label = "inverse_is_a";
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_role";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_inverse_role";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(rel_node_label + " -> " + focused_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_association";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_inverse_association";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(rel_node_label + " -> " + focused_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}


        buf.append(focused_node_label + " [").append("\n");
        buf.append("fontcolor=white,").append("\n");
        buf.append("color=red,").append("\n");
        buf.append("]").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

    public String generateGraphScript(String scheme, String version, String namespace, String code) {
		return generateGraphScript(scheme, version, namespace, code, null);
	}


    public String generateGraphScript(String scheme, String version, String namespace, String code, String[] types) {
		return generateGraphScript(scheme, version, namespace, code, types, NODES_AND_EDGES, null);
	}


    public String generateGraphScript(String scheme, String version, String namespace, String code, String[] types, int option, HashMap hmap) {
        if (types == null) {
			types = getAllRelationshipTypes();
		}
        Vector graphData = generateGraphData(scheme, version, namespace, code, types, option, hmap);
        return GraphUtils.generateGraphScript(graphData, option);
	}

    public String[] getAllRelationshipTypes() {
		String[] types = new String[6];
		types[0] = "type_superconcept";
		types[1] = "type_subconcept";
		types[2] = "type_role";
		types[3] = "type_inverse_role";
		types[4] = "type_association";
		types[5] = "type_inverse_association";
    	return types;
	}


    public Vector generateGraphData(String scheme, String version, String namespace, String code, String[] types, int option, HashMap hmap) {
		Vector graphData = new Vector();
		List typeList = null;
		if (types != null) {
			typeList = Arrays.asList(types);
		} else {
			typeList = new ArrayList();
			typeList.add("type_superconcept");
			typeList.add("type_subconcept");
    	}

		boolean useNamespace = true;
		if (gov.nih.nci.evs.browser.utils.StringUtils.isNullOrBlank(namespace)) {
			useNamespace = false;
		}


		Entity concept = new ConceptDetails(lbSvc).getConceptByCode(scheme, version, code, namespace, useNamespace);
		String name = "<NO DESCRIPTION>";
		if (concept.getEntityDescription() != null) {
			name = concept.getEntityDescription().getContent();
		}
		name = encode(name);
		if (gov.nih.nci.evs.browser.utils.StringUtils.isNullOrBlank(namespace)) {
			namespace = concept.getEntityCodeNamespace();
		}
		if (!gov.nih.nci.evs.browser.utils.StringUtils.isNullOrBlank(namespace)) {
			useNamespace = true;
		}
        String focused_node_label = getLabel(name, code);

        HashMap relMap = null;
        if (relMap == null) {
			RelationshipUtils relUtils = new RelationshipUtils(lbSvc);
			relMap = relUtils.getRelationshipHashMap(scheme, version, code, namespace, useNamespace);
	    } else {
			relMap = hmap;
		}

        HashSet nodes = new HashSet();
        nodes.add(focused_node_label);

        ArrayList list = null;

		String key = null;

		key = "type_superconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_subconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_inverse_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_inverse_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		Vector node_label_vec = new Vector();
		Iterator it = nodes.iterator();
		while (it.hasNext()) {
			String node_label = (String) it.next();
			node_label_vec.add(node_label);
		}

		key = "type_superconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					String rel_label = "is_a";
					graphData.add(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|1");
				}
			}
	    }

		key = "type_subconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					String rel_label = "is_a";
					graphData.add(rel_node_label + "|" + focused_node_label + "|" + rel_label + "|2");
				}
			}
	    }

		key = "type_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|3");
				}
			}
	    }

		key = "type_inverse_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(rel_node_label + "|" + focused_node_label + "|" +rel_label + "|4");
				}
			}
		}

		key = "type_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|5");
				}
			}
	    }

		key = "type_inverse_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(rel_node_label + "|" + focused_node_label + "|" +rel_label + "|6");
				}
			}
		}
        return graphData;
	}

    public String generateGraphScript(String scheme, String version, String namespace, String code, int option) {
        Vector graphData = generateGraphData(scheme, version, namespace, code, getAllRelationshipTypes(), option, null);
        return GraphUtils.generateGraphScript(graphData, option);
	}

    public static void main(String [] args) {
		LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
		VisUtils visUtils = new VisUtils(lbSvc);

		String codingSchemeURN = "NCI_Thesaurus";
		String codingSchemeVersion = "15.06e";
		String code = "C9118";//"Sarcoma (Code C9118)";
		String namespace = null;
		boolean useNamespace = false;

		System.out.println("scheme: " + codingSchemeURN);
		System.out.println("version: " + codingSchemeVersion);
		System.out.println("code: " + code);
		System.out.println("namespace: " + namespace);
		System.out.println("useNamespace: " + useNamespace);
		System.out.println("\n");
		String graph = visUtils.generateGraphScript(codingSchemeURN, codingSchemeVersion, namespace, code, NODES_AND_EDGES);
		System.out.println(graph);


	}
}
