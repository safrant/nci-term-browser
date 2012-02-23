<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*"%>
<%@ page import="org.LexGrid.concepts.Entity" %>
<%@ page import="gov.nih.nci.evs.browser.bean.*" %>
<%@ page import="gov.nih.nci.evs.browser.utils.*" %>
<%@ page import="gov.nih.nci.evs.browser.properties.*" %>
<%@ page import="gov.nih.nci.evs.browser.utils.*" %>
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference" %>
<%@ page import="org.apache.log4j.*" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns:c="http://java.sun.com/jsp/jstl/core">
<head>
  <title>NCI Thesaurus</title>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/styleSheet.css" />
  <link rel="shortcut icon" href="<%= request.getContextPath() %>/favicon.ico" type="image/x-icon" />
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/script.js"></script>
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/search.js"></script>
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/dropdown.js"></script>
</head>
<body>
  <script type="text/javascript"
    src="<%=request.getContextPath()%>/js/wz_tooltip.js"></script>
  <script type="text/javascript"
    src="<%=request.getContextPath()%>/js/tip_centerwindow.js"></script>
  <script type="text/javascript"
    src="<%=request.getContextPath()%>/js/tip_followscroll.js"></script>

  
  <%!
    private static Logger _logger = Utils.getJspLogger("value_set_search_results.jsp");
  %>
  <f:view>
    <!-- Begin Skip Top Navigation -->
      <a href="#evs-content" class="hideLink" accesskey="1" title="Skip repetitive navigation links">skip navigation links</A>
    <!-- End Skip Top Navigation -->  
    <%@ include file="/pages/templates/header.jsp" %>
    <div class="center-page">
      <%@ include file="/pages/templates/sub-header.jsp" %>
      <!-- Main box -->
      <div id="main-area">
      

<%

String valueSetSearch_requestContextPath = request.getContextPath();

String selected_ValueSetSearchOption = (String) request.getSession().getAttribute("selectValueSetSearchOption"); 


String message = (String) request.getSession().getAttribute("message"); 
request.getSession().removeAttribute("message"); 
Vector vsd_vec = null;

String vsd_uri = (String) request.getParameter("vsd_uri"); 

System.out.println("value_set_search_results.jsp vsd_uri: " + vsd_uri);
String selectedvalueset = null;
if (vsd_uri != null && vsd_uri.compareTo("null") != 0) { 
    String vsd_metadata = DataUtils.getValueSetDefinitionMetadata(DataUtils.findValueSetDefinitionByURI(vsd_uri));
    vsd_vec = new Vector();
    vsd_vec.add(vsd_metadata);
    //request.getSession().setAttribute("vsd_uri", vsd_uri);
} else {
    vsd_vec = (Vector) request.getSession().getAttribute("matched_vsds");
    if (vsd_vec != null && vsd_vec.size() == 1) {
	vsd_uri = (String) vsd_vec.elementAt(0);
	
	Vector temp_vec = DataUtils.parseData(vsd_uri);
	selectedvalueset = (String) temp_vec.elementAt(1);
	
	//request.getSession().setAttribute("selectedvalueset", selectedvalueset);
	//request.getSession().setAttribute("vsd_uri", selectedvalueset);
    }
}   

System.out.println("(************* vsd_uri: " + vsd_uri);

if (vsd_vec != null && vsd_vec.size() == 1) {
%>      
        <%@ include file="/pages/templates/content-header-resolvedvalueset.jsp" %>
<%
} else {
%>
        <%@ include file="/pages/templates/content-header-valueset.jsp" %>
<%
}
%>

        <div class="pagecontent">
          <a name="evs-content" id="evs-content"></a>
          <!--
          <%@ include file="/pages/templates/navigationTabs.jsp"%>
          -->
          <div class="tabTableContentContainer">
          
          <table>
            <tr>

<%
if (vsd_vec != null && vsd_vec.size() > 1) {
%>     
    <td class="texttitle-blue">Matched Value Sets:</td>
<%
}
%>
            </tr>

            <% if (message != null) { %>
        <tr class="textbodyred"><td>
      <p class="textbodyred">&nbsp;<%=message%></p>
        </td></tr>
            <% } else { %>


 <h:form id="valueSetSearchResultsForm" styleClass="search-form">            

            <tr class="textbody"><td>
 
 
 <%
 if (vsd_vec != null && vsd_vec.size() == 1) {
 %>
 <div id="message" class="textbody">
    <table border="0" width="700px">
     <tr>
       <td>
          <div class="texttitle-blue">Welcome</div>
       </td>
       
       <td class="dataCellText" align="right">
                      <h:commandButton id="Values" value="Values" action="#{valueSetBean.resolveValueSetAction}"
                        onclick="javascript:cursor_wait();"
                        image="#{valueSetSearch_requestContextPath}/images/values.gif"
                        alt="Values"
                        tabindex="3">
                      </h:commandButton>                  
                    &nbsp;
                      <h:commandButton id="versions" value="versions" action="#{valueSetBean.selectCSVersionAction}"
                        onclick="javascript:cursor_wait();"
                        image="#{valueSetSearch_requestContextPath}/images/versions.gif"
                        alt="Versions"
                        tabindex="2">
                      </h:commandButton>
                    &nbsp;
                      <h:commandButton id="xmldefinition" value="xmldefinition" action="#{valueSetBean.exportVSDToXMLAction}"
                        onclick="javascript:cursor_wait();"
                        image="#{valueSetSearch_requestContextPath}/images/xmldefinitions.gif"
                        alt="XML Definition"
                        tabindex="2">
                      </h:commandButton>
       
       </td>
     </tr>
   </table>  
   <hr/>
 </div>
 
 <%
 }
 %> 
  
 
 <%
 if (vsd_uri != null) {
  
 %>
 
    <input type="hidden" name="valueset" value="<%=vsd_uri%>">&nbsp;</input>
    
<%
 }
 %>

 
              <table class="dataTable" summary="" cellpadding="3" cellspacing="0" border="0" width="100%">

<%
if (vsd_vec != null && vsd_vec.size() > 1) {
%> 
		<th class="dataTableHeader" scope="col" align="left">&nbsp;</th>
		
		
                <th class="dataTableHeader" scope="col" align="left">Name</th>
                <!--
                <th class="dataTableHeader" scope="col" align="left">URI</th>
                <th class="dataTableHeader" scope="col" align="left">Description</th>
                -->
                <th class="dataTableHeader" scope="col" align="left">Concept Domain</th>
                <th class="dataTableHeader" scope="col" align="left">Sources</th>
		
		
<%
}
%>		
		


<%
if (vsd_vec != null) {
            for (int i=0; i<vsd_vec.size(); i++) {
            
		    String vsd_str = (String) vsd_vec.elementAt(i);
	    
		    Vector u = DataUtils.parseData(vsd_str);
		    
		    String name = (String) u.elementAt(0);
		    String uri = (String) u.elementAt(1);
		    String label = (String) u.elementAt(2);
		    String cd = (String) u.elementAt(3);
		    String sources = (String) u.elementAt(4);


if (vsd_vec.size() > 1)
{
		    if (i % 2 == 0) {
		    %>
		      <tr class="dataRowDark">
		    <%
			} else {
		    %>
		      <tr class="dataRowLight">
		    <%
			}
		    %>  
		    
<%		    
} else {
%>
    <tr>
<%
}
%>
		
<%		
if (vsd_vec != null && vsd_vec.size() > 1) {
%>		

                <td>
                <%
                if (i == 0) {
                %>
                <input type=radio name="valueset" value="<%=uri%>" checked >&nbsp;</input>
                <%
                } else {
                %>
                
		<input type=radio name="valueset" value="<%=uri%>">&nbsp;</input>
		
		<%
		}
		%>
		
		</td>
<%		
}
%>

<%
if (vsd_vec != null && vsd_vec.size() == 1) {
    //if (sources == null || sources.compareTo("") == 0) sources = "not available";
%>
		      <td class="dataCellText">
		      <p>
			 <b><%=name%></b>
		      </p>
		      
		      <p>
			 Clicking on the Values button to find all concepts contained in this value set. The value set will be resolved
			 using the production version of each participating terminology.
			 Clicking on the Versions button to view available versions of each terminology participating in this value set.
			 Clicking on XMLDefinition button to view the definition of the value set in LexGrid XML format.
		      </p>
		      
		      </td>
		      
		      <!--
		      <td class="dataCellText">
			 <%=uri%>
		      </td>
		      <td class="dataCellText">
			 <%=label%>
		      </td>
		      <td class="dataCellText">
			 <%=cd%>
		      </td>
		      <td class="dataCellText">
			 <%=sources%>
		      </td>  
		      -->
		      
		      
<%		
} else {
%>		      
		      
		      <td class="dataCellText">
                         <a href="<%=request.getContextPath() %>/pages/value_set_search_results.jsf?nav_type=valuesets&vsd_uri=<%=uri%>"><%=name%></a>
		      </td>
		      
		      <!--
		      <td class="dataCellText">
                         <a href="<%=request.getContextPath() %>/pages/value_set_search_results.jsf?nav_type=valuesets&vsd_uri=<%=uri%>"><%=uri%></a>
		      </td>


		      <td class="dataCellText">
			 <%=label%>
		      </td>
		      -->
		      <td class="dataCellText">
			 <%=cd%>
		      </td>
		      <td class="dataCellText">
			 <%=sources%>
		      </td>  

<%		
} 
%>

		      </tr>
              
              
             <%
                }
             }
             %>                 
              </table>
 
 
 
 
 <%
 if (vsd_vec != null && vsd_vec.size() > 1) {
%>
 
                   <tr><td class="dataCellText">
                     <h:commandButton id="Values" value="Values" action="#{valueSetBean.resolveValueSetAction}"
                       onclick="javascript:cursor_wait();"
                       image="#{valueSetSearch_requestContextPath}/images/values.gif"
                       alt="Values"
                       tabindex="3">
                     </h:commandButton>                  
                   &nbsp;
                     <h:commandButton id="versions" value="versions" action="#{valueSetBean.selectCSVersionAction}"
                       onclick="javascript:cursor_wait();"
                       image="#{valueSetSearch_requestContextPath}/images/versions.gif"
                       alt="Versions"
                       tabindex="2">
                     </h:commandButton>
                   &nbsp;
                     <h:commandButton id="xmldefinition" value="xmldefinition" action="#{valueSetBean.exportVSDToXMLAction}"
                       onclick="javascript:cursor_wait();"
                       image="#{valueSetSearch_requestContextPath}/images/xmldefinitions.gif"
                       alt="XML Definition"
                       tabindex="2">
                     </h:commandButton>
                  </td></tr>


<%
}
%>


<%		
if (vsd_vec != null && vsd_vec.size() == 1) {
%>		
    <input type="hidden" name="vsd_uri" id="vsd_uri" value="<%=vsd_uri%>">	
<%
}
%>

          
              <input type="hidden" name="referer" id="referer" value="<%=HTTPUtils.getRefererParmEncode(request)%>">
</h:form>
            
          </td></tr>
          
          
 <% } %>
          
          
        </table>
        </div> <!-- end tabTableContentContainer -->
        
        <%@ include file="/pages/templates/nciFooter.jsp" %>
      </div>
      <!-- end Page content -->
    </div>
    <div class="mainbox-bottom"><img src="<%=basePath%>/images/mainbox-bottom.gif" width="745" height="5" alt="Mainbox Bottom" /></div>
    <!-- end Main box -->
  </div>
</f:view>
</body>
</html>