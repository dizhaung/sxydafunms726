<%@page language="java" contentType="text/html;charset=GB2312"%>
<%@page import="java.util.List"%>
<%@page import="com.afunms.topology.dao.TreeNodeDao"%>
<%@page import="com.afunms.topology.model.TreeNode"%>
<%@page import="com.afunms.topology.util.NodeHelper"%>
<%@page import="com.afunms.common.util.SessionConstant"%>
<%@page import="com.afunms.system.model.User"%>   
<%@page import="com.afunms.config.dao.BusinessDao"%>
<%@page import="com.afunms.config.model.Business"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.afunms.indicators.util.NodeUtil"%>
<%@page import="com.afunms.indicators.model.NodeDTO"%>
<%@page import="com.afunms.event.dao.CheckEventDao"%>
<%@page import="com.afunms.indicators.util.Constant"%>
<%@page import="com.afunms.system.dao.SystemConfigDao"%>
<%
	String rootPath = request.getContextPath();
	User current_user = (User) session
			.getAttribute(SessionConstant.CURRENT_USER);
	List<Business> list = null;
	BusinessDao dao = new BusinessDao();
	try {
		list = dao.loadAll();
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		dao.close();
	}
	if (list == null) {
		list = new ArrayList();
	}
	List<Business> bussinessList_tmp = new ArrayList();
	String bids_str = ",";
	for (int i = 0; i < list.size(); i++) {
		Business business = (Business) list.get(i);
		if (current_user.getBusinessids() != null 
			&& current_user.getBusinessids().contains("," + business.getId() + ",")) {
			bussinessList_tmp.add(business);
		}
		bids_str = bids_str + business.getPid() + ",";
	}
	if(current_user.getRole() != 0) {
		list = bussinessList_tmp;
	}


%>
<html>
<head>
<title></title>
<link rel="StyleSheet" href="../performance/dtree/dtree.css" type="text/css" />
<script type="text/javascript" src="../performance/dtree/dtree.js"></script>
<style>
body {
margin-left: 6px;
margin-top: 0px;
margin-right: 6px;
margin-bottom: 6px;
scrollbar-face-color: #E0E3EB;
scrollbar-highlight-color: #E0E3EB;
scrollbar-shadow-color: #E0E3EB;
scrollbar-3dlight-color: #E0E3EB;
scrollbar-arrow-color: #7ED053;
scrollbar-track-color: #ffffff;
scrollbar-darkshadow-color: #9D9DA1;
}
body,td,th {color: #666666;line-height:20px}
.div_RightMenu
{
    z-index:30000;
    text-align:left;    
    cursor: default;
    position: absolute;
    background-color:#FAFFF8;
    width:100px;
    height:auto;
    border: 1px solid #333333;
    display:none;
    filter:progid:DXImageTransform.Microsoft.Shadow(Color=#333333,Direction=120,strength=5);    
}
.divMenuItem
{
    height:17px;
    color:Black;
    font-family:宋体;
    vertical-align:middle;
    font-size:10pt;
    margin-bottom:3px;
    cursor:hand;
    padding-left:10px;
    padding-top:2px;
}
</style>
</head>
<body>
<div class="dtree" style="">
<p><a href="javascript: d.openAll();">展开</a> | <a href="javascript: d.closeAll();">合闭</a></p>
<script type="text/javascript">
        var currTreeNodeId = '';		// 当前树的节点 Id
        var treeNodeFatherId = '';		// 当前树的节点的父 Id	
        var key = 0 ;
		d = new dTree('d');
		d.add(0,-1,' 设备资源树');
		
		<%
		
			 
			String treeshowflag_str = "0";
			SystemConfigDao systemConfigDao = new SystemConfigDao();
			try{
				treeshowflag_str = systemConfigDao.getSystemCollectByVariablename("treeshowflag");
			} catch(Exception e){
				
			} finally {
				systemConfigDao.close();
			}
			boolean treeshowflag = false;
			if("1".equals(treeshowflag_str)){
				treeshowflag = true;			// 树的显示模式 以后 可以改成  0，1， 2，。。。。。 
			}
					
			String allBusinessPid = bids_str;	// 所有具有子节点业务的id集合
			Business currBusiness = null;		// 当前业务
			String currbid = "";				// 当前业务id
			String currBusinessNodeId = "";		// 当前业务节点的Id
			List treeNodeList = null;			// 所有节点列表
			String currTreeNodeId = "";			// 当前节点Id
			boolean isShowTreeNodeFlag = true;	// 是否显示该节点 	
			boolean rightFrameFlag = true;		// 右边框架显示页面的模式 暂时只能为 true 以后添加其他模式 
					
			String currTreeNodeFatherId = "";			// 当前节点父 Id
			TreeNodeDao treeNodeDao = new TreeNodeDao();
			try{
				treeNodeList = treeNodeDao.loadAll(); // 获取所有的节点
			}catch(Exception e){
				
			} finally {
				treeNodeDao.close();
			}
			
			NodeUtil nodeUtil = new NodeUtil();
			nodeUtil.setSetedMonitorFlag(true);
			nodeUtil.setMonitorFlag("1");
			int treeNodeNum = 0;				// 树节点中 第几个
			for(Business object : list){
				// 循环每一个业务
				currBusiness = (Business)object;
				currbid = currBusiness.getId();
				if(currBusiness == null || allBusinessPid.contains(currbid)){
					continue;
				}
				
				currTreeNodeFatherId = "0";
				currTreeNodeId = "business_" + currbid;
				currBusinessNodeId = currTreeNodeId;
				%>
				 	currTreeNodeId = '<%=currTreeNodeId%>';
      				currTreeNodeFatherId = '<%=currTreeNodeFatherId%>';
				    var imagestr = "";
				    d.add(currTreeNodeId,currTreeNodeFatherId,'<%=" " + currBusiness.getName()%>',"","","","rightFrame",imagestr,imagestr);
				<%
				
				if(treeNodeList == null || treeNodeList.size() == 0){
					return;
				}
				
				// 将当前节点id 赋值给 父节点
				currTreeNodeFatherId = currTreeNodeId;
				for(Object treeNodeObject : treeNodeList){
					// 循环每一个设备树节点
					TreeNode currTreeNode = (TreeNode)treeNodeObject;
			   		List nodeList = nodeUtil.getByNodeTag(currTreeNode.getNodeTag(), currTreeNode.getCategory());
			   		List nodeDTOList = nodeUtil.conversionToNodeDTO(nodeList);
			   		
			   		if(nodeDTOList == null){
			   			nodeDTOList = new ArrayList();
			   		}
			   		
			   		List tempNodeDTOList = new ArrayList();		// 临时存储node
			   		for(Object nodeDTOObject : nodeDTOList){
			   			NodeDTO nodeDTO = (NodeDTO)nodeDTOObject;
			   			if(nodeDTO.getBusinessId().contains("," + currbid + ",")){
			   				tempNodeDTOList.add(nodeDTO);
			   			}
			   		}
			   		nodeDTOList = tempNodeDTOList;
			   		currTreeNode.setDeceiveNum(nodeDTOList.size()+"");
				 	
				 	isShowTreeNodeFlag = true;
				 	if("0".equals(currTreeNode.getDeceiveNum())){
				 		// 如果设备数为 0 则 将 显示模式的赋值给
				 		isShowTreeNodeFlag = treeshowflag;
					}
					
					// 给当前节点赋值 为 该父节点 + "_" + 该节点id;
					currTreeNodeId = currBusinessNodeId + "_" + currTreeNode.getId();
					currTreeNodeFatherId = currBusinessNodeId + "_" + currTreeNode.getFatherId();
					if(0 == currTreeNode.getFatherId()){
						currTreeNodeFatherId = currBusinessNodeId;
					}
					if(isShowTreeNodeFlag){
						%>
					 	currTreeNodeId = '<%=currTreeNodeId%>';
	      				currTreeNodeFatherId = '<%=currTreeNodeFatherId%>';
					    var imagestr = "<%=rootPath%>/performance/<%=NodeHelper.getTypeImage(currTreeNode
										.getName())%>";
					    d.add(currTreeNodeId,currTreeNodeFatherId,'<%=" " + currTreeNode.getText() + "("
								+ currTreeNode.getDeceiveNum() + ")"%>',"<%=rootPath + currTreeNode.getUrl()
								+ "&treeBid=" + currbid%>","","","rightFrame",imagestr,imagestr);
					    
						if(0 == "<%=treeNodeNum%>" && "<%=rightFrameFlag%>"){
							<%
								//首页和拓扑图点击设备时跳转的页面链接
								String rightFramePath = rootPath + request.getParameter("rightFramePath");
								rightFramePath = rightFramePath.replaceAll("-equals-","=");
								rightFramePath = rightFramePath.replaceAll("-and-","&");
								if(request.getParameter("rightFramePath") == null || request.getParameter("rightFramePath").equals("null")){
									rightFramePath = rootPath + currTreeNode.getUrl() + "&treeBid=" + currbid;
								}
							%>
							//默认情况下，让右侧窗口显示路由器性能列表，暂时作为一种性能优化的方案
							//parent.document.getElementById("rightFrame").src="<%=rightFramePath%>";
						}
						<%
						treeNodeNum++;
						currTreeNodeFatherId = currTreeNodeId;
						if("1".equals(currTreeNode.getIsHaveChild())){       
				    	// 不干任何事
				    	} else {
							for(Object nodeDTOObject : nodeDTOList){
					   			NodeDTO nodeDTO = (NodeDTO)nodeDTOObject;
					   			
					   			int alermlevel = 0;
					   			CheckEventDao checkEventDao = new CheckEventDao();
					   			try{
					   				alermlevel = checkEventDao.findMaxAlarmLevelByName(nodeDTO.getId() + ":" + nodeDTO.getType());
					   			} catch (Exception e) {
					   				
					   			} finally {
					   				checkEventDao.close();
					   			}
					   			
					   			checkEventDao.close();
					   			
					   			currTreeNodeId = currTreeNode.getNodeTag() + "_" + nodeDTO.getId();
					   			
					   			String imagestr = rootPath + "/resource/" + NodeHelper.getCurrentStatusImage(alermlevel);
					   			if(Constant.TYPE_GATEWAY.equals(nodeDTO.getType()) || Constant.TYPE_F5.equals(nodeDTO.getType()) || Constant.TYPE_VPN.equals(nodeDTO.getType()) || Constant.TYPE_HOST.equals(nodeDTO.getType()) || Constant.TYPE_NET.equals(nodeDTO.getType()) || Constant.TYPE_DB.equals(nodeDTO.getType())){
					   				imagestr = rootPath + "/performance/" + NodeHelper.getSubTypeImage(nodeDTO.getSubtype());  
					   			}   
					   			
					   			%>
							 	currTreeNodeId = '<%=currTreeNodeId%>';
			      				currTreeNodeFatherId = '<%=currTreeNodeFatherId%>';
							    var imagestr = "<%=imagestr%>";
							    d.add(currTreeNodeId,currTreeNodeFatherId,'<%=" " + nodeDTO.getName()%>',"<%=rootPath
										+ "/detail/dispatcher.jsp?flag=1&id="
										+ currTreeNode.getNodeTag()
										+ nodeDTO.getId()%>","","","rightFrame",imagestr,imagestr);
								<%
					   		} // 完成 每一个设备 循环 (L3)
					   	}
					}
				}	// 完成每一个设备树节点循环 (L2)
				
			}	// 完成每一个业务循环 (L1)
			
		%>
		document.write(d);

//------------search one device-------------根据选中的树节点在地图上搜索对应的设备

function SearchNode(ip)
{
	var coor = window.parent.mainFrame.mainFrame.getNodeCoor(ip);
	if (coor == null)
	{
		var msg = "没有在图中搜索到IP地址为 "+ ip +" 的设备。";
		window.alert(msg);
		return;
	}
	else if (typeof coor == "string")
	{
		window.alert(coor);
		return;
	}
	window.parent.mainFrame.mainFrame.moveMainLayer(coor);
}
//--------------------end--------------------
//--------------------begin选中设备显示右键菜单--------------------
var nodeid="";
var nodeip="";
var nodecategory="";
function showMenu(id,ip){
    nodeid = id.split(";")[0];
    nodecategory = id.split(";")[1];
    nodeip = ip;
    /**/
    if(document.getElementById("div_RightMenu") == null)
    {    
        CreateMenu();
        document.oncontextmenu = ShowMenu
        document.body.onclick  = HideMenu    
    }
    else
    {
        document.oncontextmenu = ShowMenu
        document.body.onclick  = HideMenu    
    } 

}
function add(){
    var nodeId = nodeid;//要保证nodeid的长度大于3
    var coor = window.parent.mainFrame.mainFrame.getNodeId(nodeId);
    if (coor == null)
	{
         window.parent.mainFrame.mainFrame.addEquip(nodeId,nodecategory);
	}
	else if (typeof coor == "string")
	{
		window.alert(coor);
		return;
	}
    window.parent.mainFrame.mainFrame.moveMainLayer(coor);
    window.alert("该设备已经在拓扑图中存在！");
}
function detail(){
    showalert(nodeid);
	window.parent.parent.opener.focus();
}
function showalert(id) {
	window.parent.parent.opener.location="/afunms/detail/dispatcher.jsp?id="+id;
}
function evtMenuOnmouseMove()
{
    this.style.backgroundColor='#8AAD77';
    this.style.paddingLeft='10px';    
}
function evtOnMouseOut()
{
    this.style.backgroundColor='#FAFFF8';
}
function CreateMenu()
{    
        var div_Menu          = document.createElement("Div");
        div_Menu.id           = "div_RightMenu";
        div_Menu.className    = "div_RightMenu";
        
        var div_Menu1         = document.createElement("Div");
        div_Menu1.id          = "div_Menu1";
        div_Menu1.className   = "divMenuItem";
        div_Menu1.onclick     = add;
        div_Menu1.onmousemove = evtMenuOnmouseMove;
        div_Menu1.onmouseout  = evtOnMouseOut;
        div_Menu1.innerHTML   = "添加到拓扑图";
        var div_Menu2         = document.createElement("Div");
        div_Menu2.id          = "div_Menu2";
        div_Menu2.className   = "divMenuItem";
        div_Menu2.onclick     = detail;
        div_Menu2.onmousemove = evtMenuOnmouseMove;
        div_Menu2.onmouseout  = evtOnMouseOut;
        div_Menu2.innerHTML   = "详细信息";
        
        div_Menu.appendChild(div_Menu1);
        div_Menu.appendChild(div_Menu2);
        document.body.appendChild(div_Menu);
}
// 判断客户端浏览器
function IsIE() 
{
    if (navigator.appName=="Microsoft Internet Explorer") 
    {
        return true;
    } 
    else 
    {
        return false;
    }
}

function ShowMenu()
{
    
    if (IsIE())
    {
        document.body.onclick  = HideMenu;
        var redge=document.body.clientWidth-event.clientX;
        var bedge=document.body.clientHeight-event.clientY;
        var menu = document.getElementById("div_RightMenu");
        if (redge<menu.offsetWidth)
        {
            menu.style.left=document.body.scrollLeft + event.clientX-menu.offsetWidth
        }
        else
        {
            menu.style.left=document.body.scrollLeft + event.clientX
            //这里有改动
            menu.style.display = "block";
        }
        if (bedge<menu.offsetHeight)
        {
            menu.style.top=document.body.scrollTop + event.clientY - menu.offsetHeight
        }
        else
        {
            menu.style.top = document.body.scrollTop + event.clientY
            menu.style.display = "block";
        }
    }
    return false;
}
function HideMenu()
{
    if (IsIE())  document.getElementById("div_RightMenu").style.display="none";    
}
</script>
</div>
</body>
</html>