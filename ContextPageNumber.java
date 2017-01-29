
public class ContextPageNumber {
	
	private String context;
	private int pageNo;
	
	public ContextPageNumber(String context, int pageNo){
		this.context = context;
		this.pageNo = pageNo;
	}
	
	public String getContext(){
		return context;
	}
	
	public int getPageNo(){
		return pageNo;
	}

}
