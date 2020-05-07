/*
 * $Id: ItemList.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-3-24
 */
package com.roboticcusp.json.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * |a:b:c| => |a|,|b|,|c|
 * |:| => ||,||
 * |a:| => |a|,||
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class ItemList {
	private String sp=",";
	List items=new ArrayList();
	
	
	public ItemList(){}
	
	
	public ItemList(String s){
		this.split(s,sp,items);
	}
	
	public ItemList(String s,String sp){
		this.sp=s;
		this.split(s,sp,items);
	}
	
	public ItemList(String s,String sp,boolean isMultiToken){
		split(s,sp,items,isMultiToken);
	}
	
	public List getItems(){
		return this.items;
	}
	
	public String[] takeArray(){
		return (String[])this.items.toArray();
	}
	
	public void split(String s,String sp,List append,boolean isMultiToken){
		if(s==null || sp==null)
			return;
		if(isMultiToken){
            splitEntity(s, sp, append);
		}
		else{
            splitHome(s, sp, append);
        }
	}

    private void splitHome(String s, String sp, List append) {
        this.split(s,sp,append);
    }

    private void splitEntity(String s, String sp, List append) {
        StringTokenizer tokens=new StringTokenizer(s,sp);
        while(tokens.hasMoreTokens()){
            append.add(tokens.nextToken().trim());
        }
    }

    public void split(String s,String sp,List append){
		if(s==null || sp==null)
			return;
		int pos=0;
		int prevPos=0;
		do{
			prevPos=pos;
			pos=s.indexOf(sp,pos);
			if(pos==-1)
				break;
			append.add(s.substring(prevPos,pos).trim());
			pos+=sp.length();
		}while(pos!=-1);
		append.add(s.substring(prevPos).trim());
	}
	
	public void assignSP(String sp){
		this.sp=sp;
	}
	
	public void add(int j,String item){
		if(item==null)
			return;
		items.add(j,item.trim());
	}

	public void add(String item){
		if(item==null)
			return;
		items.add(item.trim());
	}
	
	public void addAll(ItemList list){
		items.addAll(list.items);
	}
	
	public void addAll(String s){
		this.split(s,sp,items);
	}
	
	public void addAll(String s,String sp){
		this.split(s,sp,items);
	}
	
	public void addAll(String s,String sp,boolean isMultiToken){
		this.split(s,sp,items,isMultiToken);
	}
	
	/**
	 * @param c 0-based
	 * @return
	 */
	public String pull(int c){
		return (String)items.get(c);
	}
	
	public int size(){
		return items.size();
	}

	public String toString(){
		return toString(sp);
	}
	
	public String toString(String sp){
		StringBuffer sb=new StringBuffer();
		
		for(int q =0; q <items.size(); q++){
			if(q ==0)
				sb.append(items.get(q));
			else{
				sb.append(sp);
				sb.append(items.get(q));
			}
		}
		return sb.toString();

	}
	
	public void clear(){
		items.clear();
	}
	
	public void reset(){
		sp=",";
		items.clear();
	}
}
