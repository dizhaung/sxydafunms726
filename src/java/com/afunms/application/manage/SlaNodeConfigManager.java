/**
 * <p>Description:Tomcat Manager</p>
 * <p>Company: dhcc.com</p>
 * @author afunms
 * @project afunms
 * @date 2006-12-07
 */

package com.afunms.application.manage;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;

import com.afunms.alarm.util.AlarmIndicatorsUtil;
import com.afunms.application.dao.SlaNodeConfigDao;
import com.afunms.application.dao.WasConfigDao;
import com.afunms.application.model.SlaNodeConfig;
import com.afunms.application.model.WasConfig;
import com.afunms.application.wasmonitor.UrlConncetWas;
import com.afunms.common.base.BaseManager;
import com.afunms.common.base.ErrorMessage;
import com.afunms.common.base.ManagerInterface;
import com.afunms.common.util.CEIString;
import com.afunms.common.util.ChartGraph;
import com.afunms.common.util.DBManager;
import com.afunms.common.util.SessionConstant;
import com.afunms.common.util.SysUtil;
import com.afunms.config.dao.HaweitelnetconfDao;
import com.afunms.config.dao.SupperDao;
import com.afunms.config.dao.TimingBackupTelnetConfigDao;
import com.afunms.config.model.Huaweitelnetconf;
import com.afunms.config.model.Supper;
import com.afunms.config.model.TimingBackupTelnetConfig;
import com.afunms.event.dao.EventListDao;
import com.afunms.indicators.dao.NodeGatherIndicatorsDao;
import com.afunms.indicators.model.NodeGatherIndicators;
import com.afunms.indicators.util.NodeGatherIndicatorsUtil;
import com.afunms.polling.api.I_HostCollectData;
import com.afunms.polling.impl.HostCollectDataManager;
import com.afunms.polling.loader.WasLoader;
import com.afunms.polling.task.WasDataCollector;
import com.afunms.system.model.User;
import com.afunms.system.util.TimeGratherConfigUtil;
import com.afunms.system.util.TimeShareConfigUtil;
import com.afunms.topology.util.KeyGenerator;

public class SlaNodeConfigManager extends BaseManager implements ManagerInterface
{
	private String list()
	{
		//List ips = new ArrayList();
		User operator = (User)session.getAttribute(SessionConstant.CURRENT_USER);
		String bids = operator.getBusinessids();
		String bid[] = bids.split(",");
		Vector rbids = new Vector();
		if(bid != null && bid.length>0){
			for(int i=0;i<bid.length;i++){
				if(bid[i] != null && bid[i].trim().length()>0)
					rbids.add(bid[i].trim());
			}
		}
		SlaNodeConfigDao configdao = null;
		List list = new ArrayList();
		try{
			configdao = new SlaNodeConfigDao();
			if(operator.getRole() == 0){
				list = configdao.loadAll();
			}else{
				list = configdao.getSlaByBID(rbids);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(configdao != null){
				configdao.close();
			}
		}
		request.setAttribute("list",list);
		return "/application/sla/list.jsp";
	}
	
//	/**
//	 * 显示添加页面
//	 * 
//	 * @return
//	 */
//	private String ready_add() {
//		TimingBackupTelnetConfigDao timingBackupTelnetConfigDao = new TimingBackupTelnetConfigDao();
//		List<TimingBackupTelnetConfig> timingBackupTelnetConfigList = null;
//		try {
//			timingBackupTelnetConfigList = timingBackupTelnetConfigDao
//					.getFileList();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			timingBackupTelnetConfigDao.close();
//		}
//		request.setAttribute("timingBackupTelnetConfigList",
//				timingBackupTelnetConfigList);
//		return "/config/vpntelnet/fileBackup.jsp";
//	}
	
	/**
	 * 显示添加页面
	 * @return
	 */
	private String ready_add(){
		SupperDao supperdao = new SupperDao();
    	List<Supper> allSupper = supperdao.loadAll();
    	request.setAttribute("allSupper", allSupper);
		return "/application/sla/add.jsp";
	}
	
	// SLA添加页面中，点击选择设备，执行该方法
	private String multi_telnet_netip() {
		HaweitelnetconfDao haweitelnetconfDao = new HaweitelnetconfDao();
		List list = null;
		try {
			list = haweitelnetconfDao.getAllTelnetConfig();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			haweitelnetconfDao.close();
		}
		request.setAttribute("list", list);
		return "/application/sla/multi_telnet_netip.jsp";
	}
	
	/**
	 * 定时命令扫描日志备份
	 * 
	 * @return
	 */
	private String fileBackup() {
		TimingBackupTelnetConfigDao timingBackupTelnetConfigDao = new TimingBackupTelnetConfigDao();
		TimingBackupTelnetConfig fileBackupTelnetConfig = new TimingBackupTelnetConfig();
		
		String ipaddress = getParaValue("ipaddress");
		String poll_interval = getParaValue("poll_interval");
		int mon_flag = getParaIntValue("mon_flag");
		String name = getParaValue("name");
		String descr = getParaValue("descr");
		String slatype = getParaValue("slatype");
		int entrynumber = getParaIntValue("entrynumber");
		String bid = getParaValue("bid");
		SlaNodeConfig slanode = new SlaNodeConfig();
		List allslalist = new ArrayList();
		String[] intervals = poll_interval.split("-");
		if(ipaddress != null && ipaddress.length()>0){
			String[] ips = ipaddress.split(",");
			if(ips.length>0){
				SlaNodeConfigDao sladao = new SlaNodeConfigDao();
				try{
					for(int i=0;i<ips.length;i++){
						String _ip = ips[i];
						if(_ip != null && _ip.trim().length()>0){
							//sladao = new SlaNodeConfigDao();
							slanode.setId(sladao.getNextID());
							slanode.setTelnetconfig_id(Integer.parseInt(_ip));
							slanode.setName(name);
							slanode.setBak("");
							slanode.setBid(bid);
							slanode.setDescr(descr);
							slanode.setEntrynumber(entrynumber);
							slanode.setIntervals(Integer.parseInt(intervals[0]));
							slanode.setIntervalunit(intervals[1]);
							slanode.setMon_flag(mon_flag);
							slanode.setSlatype(slatype);
							allslalist.add(slanode);
						}
					}
				}catch(Exception e){
					
				}finally{
					sladao.close();
				}
			}
		}
		if(allslalist != null && allslalist.size()>0){
			SlaNodeConfigDao sladao = new SlaNodeConfigDao();
			try{
				sladao.save(allslalist);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return "/ciscosla.do?action=list&jp=1";
	}
	
	private String add()
    {    	   
		WasConfig vo = new WasConfig();
		
		vo.setId(KeyGenerator.getInstance().getNextKey());
		vo.setName(getParaValue("name"));
		vo.setIpaddress(getParaValue("ipaddress"));
		vo.setPortnum(getParaIntValue("portnum"));
		vo.setNodename(getParaValue("nodename"));
		vo.setServername(getParaValue("servername"));
		vo.setSendmobiles(getParaValue("sendmobiles"));
		vo.setMon_flag(getParaIntValue("mon_flag"));
		vo.setVersion(getParaValue("version"));
		vo.setSendemail(getParaValue("sendemail"));
		vo.setSupperid(getParaIntValue("supperid"));//snow add 2010-5-20

        vo.setNetid(getParaValue("bid"));
		User operator = (User)session.getAttribute(SessionConstant.CURRENT_USER);
		String bids = operator.getBusinessids();
		String bid[] = bids.split(",");
		Vector rbids = new Vector();
		if(bid != null && bid.length>0){
			for(int i=0;i<bid.length;i++){
				if(bid[i] != null && bid[i].trim().length()>0)
					rbids.add(bid[i].trim());
			}
		}
		WasConfigDao configdao = new WasConfigDao();
        try{
        	configdao.save(vo);
        	
        	
            //在轮询线程中增加被监视节点
            WasLoader loader = new WasLoader();
            try{
            	loader.loadOne(vo);
            	loader.close();
            }catch(Exception e){
            	e.printStackTrace();
            }finally{
            	loader.close();
            }
        	
        	
        	TimeShareConfigUtil timeShareConfigUtil = new TimeShareConfigUtil(); // nielin add for time-sharing at 2010-01-05
			timeShareConfigUtil.saveTimeShareConfigList(request, String.valueOf(vo.getId()), timeShareConfigUtil.getObjectType("11"));
			/* 增加采集时间设置 snow add at 2010-5-20 */
			TimeGratherConfigUtil timeGratherConfigUtil = new TimeGratherConfigUtil();
			timeGratherConfigUtil.saveTimeGratherConfigList(request, String.valueOf(vo.getId()), timeGratherConfigUtil.getObjectType("11"));
			/* snow add end*/
        
            //初始化采集指标
			try {
				NodeGatherIndicatorsUtil nodeGatherIndicatorsUtil = new NodeGatherIndicatorsUtil();
				nodeGatherIndicatorsUtil.addGatherIndicatorsForNode(vo.getId()+"", "middleware", "was","1");
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//初始化指标阀值
			try {
				AlarmIndicatorsUtil alarmIndicatorsUtil = new AlarmIndicatorsUtil();
				alarmIndicatorsUtil.saveAlarmInicatorsThresholdForNode(String.valueOf(vo.getId()), "middleware", "was");
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			NodeGatherIndicatorsDao indicatorsdao = new NodeGatherIndicatorsDao();
	    	List<NodeGatherIndicators> monitorItemList = new ArrayList<NodeGatherIndicators>(); 	
	    	Hashtable<String,Hashtable<String,NodeGatherIndicators>> urlHash = new Hashtable<String,Hashtable<String,NodeGatherIndicators>>();//存放需要监视的DB2指标  <dbid:Hashtable<name:NodeGatherIndicators>>

	    	try{
	    		//获取被启用的WAS所有被监视指标
	    		monitorItemList = indicatorsdao.getByNodeId(vo.getId()+"",1,"middleware","was");
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		indicatorsdao.close();
	    	}
	    	if(monitorItemList == null)monitorItemList = new ArrayList<NodeGatherIndicators>();
	    	Hashtable gatherHash = new Hashtable();
	    	for(int i=0;i<monitorItemList.size();i++){
	    		NodeGatherIndicators nodeGatherIndicators = (NodeGatherIndicators)monitorItemList.get(i);
	    		gatherHash.put(nodeGatherIndicators.getName(), nodeGatherIndicators);
	    	}
	        
	    	//将采集代码注销，添加时不采集
//	    	WasDataCollector wasdata = new WasDataCollector();
//	        try{
//	        	wasdata.collect_data(vo.getId()+"", gatherHash);
//	        }catch(Exception e){
//	        	
//	        }
	    	//保存应用
			HostApplyManager.save(vo);
        }catch(Exception e){
			e.printStackTrace();
		}finally{
			configdao.close();
		}
        configdao = new WasConfigDao();
		List list = new ArrayList();
		try{
			if(operator.getRole() == 0){
				list = configdao.loadAll();
			}else{
				list = configdao.getWasByBID(rbids);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			configdao.close();
		}
		
		request.setAttribute("list",list);
		return "/application/was/list.jsp";
    }    
	
	
	public String delete()
	{
		String[] ids = getParaArrayValue("checkbox");
		SlaNodeConfig vo = new SlaNodeConfig();
		List list = new ArrayList();	
    	if(ids != null && ids.length > 0){	
    		SlaNodeConfigDao configdao = new SlaNodeConfigDao();
    		try{
    			for(int i=0;i<ids.length;i++){
    				//删除性能表数据
    			}
    			configdao.delete(ids);
    		}catch(Exception e){
    			e.printStackTrace();
    		}finally{
    			configdao.close();
    		}
    	}
		User operator = (User)session.getAttribute(SessionConstant.CURRENT_USER);
		String bids = operator.getBusinessids();
		String bid[] = bids.split(",");
		Vector rbids = new Vector();
		if(bid != null && bid.length>0){
			for(int i=0;i<bid.length;i++){
				if(bid[i] != null && bid[i].trim().length()>0)
					rbids.add(bid[i].trim());
			}
		}
		SlaNodeConfigDao configdao = null;
		//List list = new ArrayList();
		try{
			configdao = new SlaNodeConfigDao();
			if(operator.getRole() == 0){
				list = configdao.loadAll();
			}else{
				list = configdao.getSlaByBID(rbids);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(configdao != null){
				configdao.close();
			}
		}
		request.setAttribute("list",list);
		return "/application/sla/list.jsp";
		//return list();
	}
	
	/**
	 * @author nielin add for time-sharing at 2010-01-05
	 * @return
	 */
	private String ready_edit(){
		String jsp = "/application/sla/edit.jsp";
		//List timeShareConfigList = new ArrayList();
		SlaNodeConfigDao dao = new SlaNodeConfigDao();
		SlaNodeConfig vo = null;
		try{
			//setTarget(jsp);
			//jsp = readyEdit(dao);
//			TimeShareConfigUtil timeShareConfigUtil = new TimeShareConfigUtil();
//			timeShareConfigList = timeShareConfigUtil.getTimeShareConfigList(getParaValue("id"), timeShareConfigUtil.getObjectType("11"));
//		    /* 获得设备的采集时间 snow add at 2010-05-20 */
////			//提供供应商信息
////			SupperDao supperdao = new SupperDao();
////	    	List<Supper> allSupper = supperdao.loadAll();
////	    	request.setAttribute("allSupper", allSupper);
//	    	//提供已设置的采集时间信息
//	    	TimeGratherConfigUtil tg  = new TimeGratherConfigUtil();
//	    	List<TimeGratherConfig> timeGratherConfigList = tg.getTimeGratherConfig(getParaValue("id"), tg.getObjectType("11"));
//	    	for (TimeGratherConfig timeGratherConfig : timeGratherConfigList) {
//	    		timeGratherConfig.setHourAndMin();
//			}
//	    	request.setAttribute("timeGratherConfigList", timeGratherConfigList);  
//	    	/* snow end */
			
		       //BaseVo vo = null;
		       try{
		    	   //String ii=getParaValue("id");
		    	   vo = (SlaNodeConfig)dao.findByID(getParaValue("id"));       
		       }catch(Exception e){
		    	   e.printStackTrace();
		       }finally{
		    	   
		       }
		       if(vo!=null)
		       {	   
		    	   HaweitelnetconfDao haweitelnetconfDao = new HaweitelnetconfDao();
		    	   Huaweitelnetconf telconf = new Huaweitelnetconf();
		    	   try{
		    		   telconf = (Huaweitelnetconf)haweitelnetconfDao.findByID(vo.getTelnetconfig_id()+"");
		    	   }catch(Exception e){
		    		   e.printStackTrace();
		    	   }finally{
		    		   haweitelnetconfDao.close();
		    	   }
		          request.setAttribute("vo",vo);
		          request.setAttribute("ipaddress", telconf.getIpaddress());
		          //targetJsp = getTarget();
		       }
		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		//request.setAttribute("timeShareConfigList", timeShareConfigList);
	    return jsp;
	}
	
	private String update()
    {    	   
		SlaNodeConfig vo = new SlaNodeConfig();
		
		List list = new ArrayList();
		List conflist = new ArrayList();
		List ips = new ArrayList();
				
		String poll_interval = getParaValue("poll_interval");
		String[] intervals = poll_interval.split("-");
		int id = getParaIntValue("id");
		
    	vo.setId(getParaIntValue("id"));
		vo.setName(getParaValue("name"));
		vo.setDescr(getParaValue("descr"));
		vo.setSlatype(getParaValue("slatype"));
		vo.setIntervals(Integer.parseInt(intervals[0]));
		vo.setIntervalunit(intervals[1]);
		vo.setMon_flag(getParaIntValue("mon_flag"));		
        vo.setBid(getParaValue("bid"));
        SlaNodeConfigDao configdao = new SlaNodeConfigDao();
        try{
        	vo = (SlaNodeConfig)configdao.findByID(id+"");
        	vo.setName(getParaValue("name"));
    		vo.setDescr(getParaValue("descr"));
    		vo.setSlatype(getParaValue("slatype"));
    		vo.setIntervals(Integer.parseInt(intervals[0]));
    		vo.setIntervalunit(intervals[1]);
    		vo.setMon_flag(getParaIntValue("mon_flag"));		
            vo.setBid(getParaValue("bid"));
        	
        	try{
        		configdao.update(vo);	
//        		TimeShareConfigUtil timeShareConfigUtil = new TimeShareConfigUtil();
//            	timeShareConfigUtil.saveTimeShareConfigList(request, String.valueOf(vo.getId()), timeShareConfigUtil.getObjectType("11"));
//            	/* 增加采集时间设置 snow add at 2010-5-20 */
//            	TimeGratherConfigUtil timeGratherConfigUtil = new TimeGratherConfigUtil();
//            	timeGratherConfigUtil.saveTimeGratherConfigList(request, String.valueOf(vo.getId()), timeGratherConfigUtil.getObjectType("11"));
//            	/* snow add end*/
        	
        	}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
			
			User operator = (User)session.getAttribute(SessionConstant.CURRENT_USER);
			String bids = operator.getBusinessids();
			String bid[] = bids.split(",");
			Vector rbids = new Vector();
			if(bid != null && bid.length>0){
				for(int i=0;i<bid.length;i++){
					if(bid[i] != null && bid[i].trim().length()>0)
						rbids.add(bid[i].trim());
				}
			}
			configdao = new SlaNodeConfigDao();
			try{
				if(operator.getRole() == 0 ){
					list = configdao.loadAll();
				}else{
					list = configdao.getSlaByBID(rbids);
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
			
        }catch(Exception e){
        	e.printStackTrace();
        }
       
        
		request.setAttribute("list",list);
             
		return "/application/sla/list.jsp";
    }
	
	private String search()
    {    	   
//		DBVo vo = new DBVo();
//		DBDao dao = new DBDao();
//		SybspaceconfigDao configdao = new SybspaceconfigDao();
//		List list = new ArrayList();
//		List conflist = new ArrayList();
//		List ips = new ArrayList();
//		String ipaddress ="";
//
//		try{
//			ipaddress = getParaValue("ipaddress");
//			User operator = (User)session.getAttribute(SessionConstant.CURRENT_USER);
//			String bids = operator.getBusinessids();
//			String bid[] = bids.split(",");
//			Vector rbids = new Vector();
//			if(bid != null && bid.length>0){
//				for(int i=0;i<bid.length;i++){
//					if(bid[i] != null && bid[i].trim().length()>0)
//						rbids.add(bid[i].trim());
//				}
//			}
//
//			List oraList = new ArrayList();
//			DBTypeDao typedao = new DBTypeDao();
//			DBTypeVo typevo = (DBTypeVo)typedao.findByDbtype("sybase");
//			try{
//				oraList = dao.getDbByTypeAndBID(typevo.getId(),rbids);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			
//			if(oraList != null && oraList.size()>0){
//				for(int i=0;i<oraList.size();i++){
//					DBVo dbmonitorlist = (DBVo)oraList.get(i);
//					ips.add(dbmonitorlist.getIpAddress());
//				}
//			}
//			
//			
//			configdao = new SybspaceconfigDao();
//			//configdao.fromLastToOraspaceconfig();
//			
//			//ipaddress = (String)session.getAttribute("ipaddress");			
//			if (ipaddress != null && ipaddress.trim().length()>0){
//				configdao = new SybspaceconfigDao();
//				list =configdao.getByIp(ipaddress);
//				if (list == null || list.size() == 0){
//					list = configdao.loadAll();
//				}
//			}else{
//				configdao = new SybspaceconfigDao();
//				list = configdao.loadAll();		
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		//request.setAttribute("Oraspaceconfiglist", conflist);
//		request.setAttribute("iplist",ips);
//		request.setAttribute("ipaddress",ipaddress);
//		configdao = new SybspaceconfigDao();
//		list = configdao.getByIp(ipaddress);
//		request.setAttribute("list",list);
		return "/application/db/sybaseconfigsearchlist.jsp";
    }
	
	private String addalert()
    {    
		WasConfig vo = new WasConfig();
		
		List list = new ArrayList();
		List conflist = new ArrayList();
		List ips = new ArrayList();
		String ipaddress ="";
		try{
			WasConfigDao configdao = new WasConfigDao();
			try{
				vo = (WasConfig)configdao.findByID(getParaValue("id"));
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
			
			vo.setMon_flag(1);
			configdao = new WasConfigDao();
			try{
				configdao.update(vo);	
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
			
			User operator = (User)session.getAttribute(SessionConstant.CURRENT_USER);
			String bids = operator.getBusinessids();
			String bid[] = bids.split(",");
			Vector rbids = new Vector();
			if(bid != null && bid.length>0){
				for(int i=0;i<bid.length;i++){
					if(bid[i] != null && bid[i].trim().length()>0)
						rbids.add(bid[i].trim());
				}
			}
			configdao = new WasConfigDao();
			try{
				list = configdao.getWasByBID(rbids);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		request.setAttribute("list",list);
		return "/application/was/list.jsp";
    }
	
	private String cancelalert()
    {    
		WasConfig vo = new WasConfig();
		
		List list = new ArrayList();
		List conflist = new ArrayList();
		List ips = new ArrayList();
		String ipaddress ="";
		try{
			WasConfigDao configdao = new WasConfigDao();
			try{
				vo = (WasConfig)configdao.findByID(getParaValue("id"));
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
			
			vo.setMon_flag(0);
			configdao = new WasConfigDao();
			try{
				configdao.update(vo);	
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
			
			User operator = (User)session.getAttribute(SessionConstant.CURRENT_USER);
			String bids = operator.getBusinessids();
			String bid[] = bids.split(",");
			Vector rbids = new Vector();
			if(bid != null && bid.length>0){
				for(int i=0;i<bid.length;i++){
					if(bid[i] != null && bid[i].trim().length()>0)
						rbids.add(bid[i].trim());
				}
			}
			configdao = new WasConfigDao();
			try{
				list = configdao.getWasByBID(rbids);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				configdao.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		request.setAttribute("list",list);
		return "/application/was/list.jsp";
    }
	
	private String sychronizeData()
    {    
		
		int queryid = getParaIntValue("id");
		String dbpage = getParaValue("dbPage");
		NodeGatherIndicatorsDao indicatorsdao = new NodeGatherIndicatorsDao();
    	List<NodeGatherIndicators> monitorItemList = new ArrayList<NodeGatherIndicators>(); 	
    	Hashtable<String,Hashtable<String,NodeGatherIndicators>> urlHash = new Hashtable<String,Hashtable<String,NodeGatherIndicators>>();//存放需要监视的DB2指标  <dbid:Hashtable<name:NodeGatherIndicators>>

    	try{
    		//获取被启用的所有被监视指标
    		monitorItemList = indicatorsdao.getByNodeId(queryid+"", 1,"middleware","was");
    	}catch(Exception e){
    		e.printStackTrace();
    	}finally{
    		indicatorsdao.close();
    	}
    	if(monitorItemList == null)monitorItemList = new ArrayList<NodeGatherIndicators>();
    	Hashtable gatherHash = new Hashtable();
    	for(int i=0;i<monitorItemList.size();i++){
    		NodeGatherIndicators nodeGatherIndicators = (NodeGatherIndicators)monitorItemList.get(i);
			gatherHash.put(nodeGatherIndicators.getName(), nodeGatherIndicators);
    	}
		
        try {                	
        	WasDataCollector wascollector = new WasDataCollector();
        	wascollector.collect_data(queryid+"", gatherHash);
        }catch(Exception exc){
        	
        }

        if("detail".equalsIgnoreCase(dbpage)){
        	return "/was.do?action=detail&id="+queryid;
        }else if("jdbcdetail".equalsIgnoreCase(dbpage)){
            	return "/was.do?action=jdbcdetail&id="+queryid;
        }else if("session".equalsIgnoreCase(dbpage)){
        	return "/was.do?action=session&id="+queryid;
        }else if("system".equalsIgnoreCase(dbpage)){
        	return "/was.do?action=system&id="+queryid;
        }else if("cache".equalsIgnoreCase(dbpage)){
        	return "/was.do?action=cache&id="+queryid;
        }else if("service".equalsIgnoreCase(dbpage)){
        	return "/was.do?action=service&id="+queryid;
        }else if("orb".equalsIgnoreCase(dbpage)){
        	return "/was.do?action=orb&id="+queryid;
        }else
        	return "/was.do?action=event&id="+queryid;
		//return "/application/web/detail.jsp";
    }
	
	private String detail()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		request.setAttribute("was", vo);
	    if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/detail5.jsp";
		}else{
			return "/application/was/detail7.jsp";
		}
    }
	private String jdbcdetail()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		request.setAttribute("was", vo);
		if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/was5_jdbc.jsp";
		}else{
			return "/application/was/was7_jdbc.jsp";
		}
    }
	private String session()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		request.setAttribute("was", vo);
		if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/was5_servlet.jsp";
		}else{
			return "/application/was/was7_servlet.jsp";
		}
    }
	private String system()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		request.setAttribute("was", vo);
		if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/was5_jvm.jsp";
		}else{
			return "/application/was/was7_jvm.jsp";
		}
    }
	private String orb()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		request.setAttribute("was", vo);
		if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/was5_trans.jsp";
		}else{
			return "/application/was/was7_trans.jsp";
		}
    }
	
	private String service()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		request.setAttribute("was", vo);
		if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/was5_thread.jsp";
		}else{
			return "/application/was/was7_thread.jsp";
		}
    }
	private String cache()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		request.setAttribute("was", vo);
		if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/was5_cache.jsp";
		}else{
			return "/application/was/was7_cache.jsp";
		}
    }
	private String event()
    {    
		WasConfig vo = new WasConfig();
		WasConfigDao dao = new WasConfigDao();
		List list = new ArrayList();
		try{
			vo = (WasConfig)dao.findByID(getParaValue("id"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		
    	//Hashtable hash = null;
//    	weblogicsnmp = new WeblogicSnmp(weblogicconf.getIpAddress(),weblogicconf.getCommunity(),weblogicconf.getPortnum());
//    	hash=weblogicsnmp.collectData();
    
    	//hash = (Hashtable)ShareData.getWeblogicdata().get(vo.getIpaddress());
    	try{
    	
    		int	status = getParaIntValue("status");
	    	int	level1 = getParaIntValue("level1");
	    	if(status == -1)status=99;
	    	if(level1 == -1)level1=99;
	    	request.setAttribute("status", status);
	    	request.setAttribute("level1", level1);
	    	
	    	String	b_time = getParaValue("startdate");
	    	String	t_time = getParaValue("todate");
	    	
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			if (b_time == null){
				b_time = sdf1.format(new Date());
			}
			if (t_time == null){
				t_time = sdf1.format(new Date());
			}
			String starttime1 = b_time + " 00:00:00";
			String totime1 = t_time + " 23:59:59";
			request.setAttribute("startdate", b_time);
			request.setAttribute("todate", t_time);
			try{
				User user = (User)session.getAttribute(SessionConstant.CURRENT_USER);      //用户姓名
				//SysLogger.info("user businessid===="+vo.getBusinessids());
				EventListDao eventdao = new EventListDao();
				list = eventdao.getQuery(starttime1,totime1,"wasserver",status+"",level1+"",
						user.getBusinessids(),vo.getId());
				
				//ConnectUtilizationhash = hostmanager.getCategory(host.getIpAddress(),"Ping","ConnectUtilization",starttime1,totime1);
			}catch(Exception ex){
				ex.printStackTrace();
			}
	        
	}catch(Exception e){
		e.printStackTrace();
	}finally{
		//weblogicconfigdao.close();
	}
	    request.setAttribute("was", vo);
    	//request.setAttribute("id", id);
    	//request.setAttribute("hash", hash);
    	request.setAttribute("list", list);
    	//request.setAttribute("flag", flag);
    	if(vo.getVersion().equalsIgnoreCase("V5")){
			return "/application/was/was5_event.jsp";
		}else{
			return "/application/was/was7_event.jsp";
		}
    }
	public String execute(String action) 
	{	
        if(action.equals("list"))
            return list();    
        if(action.equals("ready_add"))
        	return ready_add();
        if(action.equals("add"))
        	return add();
        if(action.equals("multi_telnet_netip"))
        	return multi_telnet_netip();
		if (action.equals("fileBackup")) { // 定时扫描命令配置
			return fileBackup();
		}
        if(action.equals("delete"))
            return delete();
        if(action.equals("ready_edit"))
        	return ready_edit();
        if(action.equals("update"))
            return update();
        if(action.equals("addalert"))
            return addalert();
        if(action.equals("cancelalert"))
            return cancelalert();
        if(action.equals("detail"))
            return detail();
        if(action.equals("search"))
            return search();
        if(action.equals("jdbcdetail"))
            return jdbcdetail();
        if(action.equals("session"))
            return session();
        if(action.equals("system"))
            return system();
        if(action.equals("cache"))
            return cache();
        if(action.equals("service"))
            return service();
        if(action.equals("orb"))
            return orb();
        if(action.equals("event"))
            return event();
        if(action.equals("sychronizeData"))
            return sychronizeData();
        if(action.equals("isOK"))
            return isOK();
        
		setErrorCode(ErrorMessage.ACTION_NO_FOUND);
		return null;
	}
	
	private String isOK()
    {    
		
		int queryid = getParaIntValue("id");
		WasConfig wasconf = null;              	
        int serverflag = 0;
        String ipaddress = "";
        WasConfigDao dao=new WasConfigDao();
		try{
			wasconf=(WasConfig)dao.findByID(queryid+"");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
      //  AdminClient5 wasadmin = new AdminClient5();
        UrlConncetWas  conWas = new UrlConncetWas();	
		//对可用性进行检测
		boolean collectWasIsOK = true;
		try{
			
				collectWasIsOK = conWas.connectWasIsOK(wasconf.getIpaddress(),wasconf.getPortnum());
		}catch(Exception e){
			e.printStackTrace();
		}

		request.setAttribute("isOK", collectWasIsOK);
		request.setAttribute("name", wasconf.getName());
		request.setAttribute("str", wasconf.getIpaddress());
        return "/tool/wasisok.jsp";
    }
	
	private void drawPiechart(String[] keys,String[] values,String chname,String enname){
		ChartGraph cg = new ChartGraph();
		DefaultPieDataset piedata = new DefaultPieDataset();
		for(int i=0;i<keys.length;i++){
		  piedata.setValue(keys[i], new Double(values[i]).doubleValue());
		}
		cg.pie(chname,piedata,enname,300,120);    	
	}
	
	private void drawchart(Minute[] minutes,String keys,String[][] values,String chname,String enname){
		try{
		//int size = keys.length;
		TimeSeries[] s2 = new TimeSeries[1];
		String[] keymemory = new String[1];
		String[] unitMemory = new String[1];
		//for(int i = 0 ; i < size ; i++){
			String key = keys;
		//	System.out.println("in drawchart -------------- i="+i+" key="+key+" ");
			TimeSeries ss2 = new TimeSeries(key,Minute.class);
			String[] hmemory = values[0];
			arrayTochart(ss2,hmemory,minutes);
			keymemory[0] = key;
			s2[0] = ss2;
	//		}
		ChartGraph cg = new ChartGraph();
		cg.timewave(s2,"x","y(MB)",chname,enname,300,150);    	
		}catch(Exception e){
		System.out.println("drawchart error:" + e);
	}
	}
	
	private void arrayTochart(TimeSeries s,String[] h,Minute[] minutes){
		try{
		for(int j=0;j<h.length;j++){
			//System.out.println("h[i]: " + h[j]);
			String value=h[j];
			Double v=new Double(0);
			if(value!=null)	 {
				v=new Double(value);
			}
			s.addOrUpdate(minutes[j],v);
			}
		}catch(Exception e){
			System.out.println("arraytochart error:" + e);
		}
	}
	public String getF(String s){
		if(s.length()>5)
			s=s.substring(0,5);
		return s;
	}
	
	private void p_draw_line(Hashtable hash,String title1,String title2,int w,int h){
		List list = (List)hash.get("list");
		try{
		if(list==null || list.size()==0){
			draw_blank(title1,title2,w,h);
		}
		else{
		String unit = (String)hash.get("unit");
		if (unit == null)unit="%";
		ChartGraph cg = new ChartGraph();
		TimeSeries ss = new TimeSeries(title1,Minute.class);
		TimeSeries[] s = {ss};
		for(int j=0; j<list.size(); j++){
			//if (title1.equals("Cpu利用率")){
				Vector v = (Vector)list.get(j);
				//CPUcollectdata obj = (CPUcollectdata)list.get(j);
				Double	d=new Double((String)v.get(0));			
				String dt = (String)v.get(1);			
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date time1 = sdf.parse(dt);				
				Calendar temp = Calendar.getInstance();
				temp.setTime(time1);
				Minute minute=new Minute(temp.get(Calendar.MINUTE),temp.get(Calendar.HOUR_OF_DAY),temp.get(Calendar.DAY_OF_MONTH),temp.get(Calendar.MONTH)+1,temp.get(Calendar.YEAR));
				ss.addOrUpdate(minute,d);
			//}
		}
		cg.timewave(s,"x(时间)","y("+unit+")",title1,title2,w,h);
		}
		hash = null;
		}
		catch(Exception e){e.printStackTrace();}
		}

	private void draw_blank(String title1,String title2,int w,int h){
		ChartGraph cg = new ChartGraph();
		TimeSeries ss = new TimeSeries(title1,Minute.class);
		TimeSeries[] s = {ss};
		try{
			Calendar temp = Calendar.getInstance();
			Minute minute=new Minute(temp.get(Calendar.MINUTE),temp.get(Calendar.HOUR_OF_DAY),temp.get(Calendar.DAY_OF_MONTH),temp.get(Calendar.MONTH)+1,temp.get(Calendar.YEAR));
			ss.addOrUpdate(minute,null);
			cg.timewave(s,"x(时间)","y",title1,title2,w,h);
		}
		catch(Exception e){e.printStackTrace();}
	}
	//quzhi add
	public double wasping(int id)
    {    	   
    	String strid = String.valueOf(id);
		WasConfig vo = new WasConfig();
		
		double avgpingcon=0;
		Hashtable sysValue = new Hashtable();
		Hashtable imgurlhash=new Hashtable();
		Hashtable maxhash = new Hashtable();
		String pingconavg ="0";
		try{
			WasConfigDao dao = new WasConfigDao();
			try{
				vo = (WasConfig)dao.findByID(strid);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				dao.close();
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String time1 = sdf.format(new Date());
			String newip=SysUtil.doip(vo.getIpaddress());						
		
			String starttime1 = time1 + " 00:00:00";
			String totime1 = time1 + " 23:59:59";
			
			Hashtable ConnectUtilizationhash = new Hashtable();
			I_HostCollectData hostmanager=new HostCollectDataManager();
			try{
				ConnectUtilizationhash = getCategory(vo.getIpaddress(),"WasPing","ConnectUtilization",starttime1,totime1);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			if (ConnectUtilizationhash.get("avgpingcon")!=null)
				pingconavg = (String)ConnectUtilizationhash.get("avgpingcon");
			if(pingconavg != null){
				pingconavg = pingconavg.replace("%", "");
			}
			  avgpingcon = new Double(pingconavg+"").doubleValue();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			//dao.close();
		}
		return avgpingcon;
    } 
	
	//quzhi add
	public Hashtable getCategory(
			String ip,
			String category,
			String subentity,
			String starttime,
			String endtime)
			throws Exception {
			Hashtable hash = new Hashtable();
		 	DBManager dbmanager = new DBManager();
		 	ResultSet rs = null;
			try{
				//con=DataGate.getCon();
				if (!starttime.equals("") && !endtime.equals("")) {
					//con=DataGate.getCon();
//					String ip1 ="",ip2="",ip3="",ip4="";	
//					String tempStr = "";
//					String allipstr = "";
//					if (ip.indexOf(".")>0){
//						ip1=ip.substring(0,ip.indexOf("."));
//						ip4=ip.substring(ip.lastIndexOf(".")+1,ip.length());			
//						tempStr = ip.substring(ip.indexOf(".")+1,ip.lastIndexOf("."));
//					}
//					ip2=tempStr.substring(0,tempStr.indexOf("."));
//					ip3=tempStr.substring(tempStr.indexOf(".")+1,tempStr.length());
//					allipstr=ip1+ip2+ip3+ip4;
					String allipstr = SysUtil.doip(ip);
					
					String sql = "";
					StringBuffer sb = new StringBuffer();
					 if (category.equals("WasPing")){
						sb.append(" select h.thevalue,DATE_FORMAT(h.collecttime,'%Y-%m-%d %H:%i:%s') as collecttime,h.unit from wasping"+allipstr+" h where ");
					 }
					sb.append(" h.category='");
					sb.append(category);
					sb.append("' and h.subentity='");
					sb.append(subentity);
					sb.append("' and h.collecttime >= '");
					sb.append(starttime);
					sb.append("' and h.collecttime <= '");
					sb.append(endtime);
					sb.append("' order by h.collecttime");
					sql = sb.toString();
//					SysLogger.info(sql);
					
					rs = dbmanager.executeQuery(sql);
					List list1 =new ArrayList();
					String unit = "";
					String max = "";
					double tempfloat=0;
					double pingcon = 0;
					double cpucon = 0;
					int downnum = 0;
					int i=0;
			        while (rs.next()) {
			        	i=i+1;
			        	Vector v =new Vector();		        	
			            String thevalue=rs.getString("thevalue");
			            String collecttime = rs.getString("collecttime");		            
			            v.add(0,emitStr(thevalue));
			            v.add(1,collecttime);
			            v.add(2,rs.getString("unit"));
			            if (category.equals("WasPing")&&subentity.equalsIgnoreCase("ConnectUtilization")){
			            	pingcon=pingcon+getfloat(thevalue);
			            	if(thevalue.equals("0")){
			            		downnum = downnum + 1;
			            	}	
			            }
			            if (subentity.equalsIgnoreCase("ConnectUtilization")) {
			            	if (i==1)tempfloat = getfloat(thevalue);
			            	if (tempfloat > getfloat(thevalue))tempfloat = getfloat(thevalue);
			            }else{
			            	if (tempfloat < getfloat(thevalue))tempfloat = getfloat(thevalue);
			            }
			            list1.add(v);	
			    }	
			        rs.close();
			        //stmt.close();
			        
					Integer size = new Integer(0);
					hash.put("list", list1);
					if (list1.size() != 0) {
						size = new Integer(list1.size());
						if (list1.get(0) != null) {
							Vector tempV = (Vector)list1.get(0);
							unit = (String)tempV.get(2);
						}
					}
					if (category.equals("WasPing")&&subentity.equalsIgnoreCase("ConnectUtilization")){
						if (list1 !=null && list1.size()>0){
							hash.put("avgpingcon", CEIString.round(pingcon/list1.size(),2)+unit);						
							hash.put("pingmax", tempfloat+"");
							hash.put("downnum", downnum+"");
						}else{ 
							hash.put("avgpingcon", "0.0%");	
							hash.put("pingmax", "0.0%");
							hash.put("downnum", "0");
						}
					}
					hash.put("size", size);			
					hash.put("max", CEIString.round(tempfloat,2) + unit);
					hash.put("unit", unit);
			        }
				} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if (rs != null)
				rs.close();
				dbmanager.close();
			}
			
			return hash;
		}
	
	 private String emitStr(String num) {
			if (num != null) {
				if (num.indexOf(".")>=0){				
					if (num.substring(num.indexOf(".")+1).length()>7){
						String tempStr = num.substring(num.indexOf(".")+1);
						num = num.substring(0,num.indexOf(".")+1)+tempStr.substring(0,7);					
					}
				}
			}
			return num;
	 }
	 
	 private double getfloat(String num) {
		 double snum = 0.0;
		 if (num != null) {
			 if (num.indexOf(".")>=0){				
				 if (num.substring(num.indexOf(".")+1).length()>7){
					 String tempStr = num.substring(num.indexOf(".")+1);
					 num = num.substring(0,num.indexOf(".")+1)+tempStr.substring(0,7);					
				 }
			 }
			 int inum = (int) (Float.parseFloat(num) * 100);
			 snum = new Double(inum/100.0).doubleValue();
		 }
		 return snum;
	 }
	
	
//quzhi
public List getInfoByFlag(Integer flag) throws Exception{
		
		List list = new ArrayList();
		WasConfigDao dao=new WasConfigDao();
		try{
			list=dao.getWasByFlag(flag);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dao.close();
		}
		
		return list;
	}
	
}