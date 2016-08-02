package OOP;

import java.util.*;
import java.text.*;

enum CAR_SIZE{
	SMALL,MEDIUM,BIG;
}

public class ParkingSystem {
	private static ParkingSystem instance = null;
	private FeeManagement feesys;
	private List<Lot>[] freeLot;
	private Map<Car,Lot> occupys;
	private Map<CAR_SIZE,Integer> indexs;
	private int[] prices;
	private ParkingSystem(){
		feesys = new FeeManagement();
		freeLot = new LinkedList[CAR_SIZE.values().length];
		indexs = new HashMap<>();
		prices = new int[CAR_SIZE.values().length];
		prices[0]=10;
		for(int i=1;i<prices.length;i++){
			prices[i]=prices[i-1]*2;
		}
		int i=0;
		for(CAR_SIZE size:CAR_SIZE.values()){
			indexs.put(size, i);
			freeLot[i] = new LinkedList<>();
			for(int j=0;j<2;j++){
				Lot lot = LotFactory.genLot(j, size);
				freeLot[i].add(lot);
			}
			i++;
		}
		occupys = new HashMap<>();

	}
	public static ParkingSystem getInstance(){
		if(instance==null) instance = new ParkingSystem();
		return instance;
	}
	
	private int getIndex(CAR_SIZE size){
		if(!indexs.containsKey(size)) return -1;
		return indexs.get(size);
	}
	private int getLotIndex(int ind){
		int i=ind;
		for(;i<freeLot.length;i++){
			if(!freeLot[i].isEmpty()) return i;
		}
		return i;
	}
	public boolean checkIn(Car car){
		if(occupys.containsKey(car)){
			System.out.println("Already check in");
			return false;			
		}
		int ind = getIndex(car.getCarSize());
		if(ind==-1){
			System.out.println("error size");
			return false;
		}
		else{
			int lotIndex = getLotIndex(ind);
			if(lotIndex>=freeLot.length){
				System.out.println("PARKING IS FULL");
				return false;				
			}
			else{
				Lot lot = freeLot[lotIndex].remove(0);
				lot.checkin(car);
				System.out.println(car.getlicence()+" checked in");
				occupys.put(car, lot);
				return true;
			}
		}
	}
	public boolean checkOut(Car car){
		if(!occupys.containsKey(car)){
			System.out.println("No such car");
			return false;			
		}
		else{
			Lot lot = occupys.get(car);
			int carind = getIndex(car.getCarSize());
			Recipe rec = lot.checkout(prices[carind]);
			feesys.collectMoney(rec);
			System.out.println(car.getlicence()+" checked out");
			int lotindex = getIndex(lot.getLotSize());
			freeLot[lotindex].add(lot);
			occupys.remove(car);
			return true;
		}
	}
	
	public void genRecipeReport(){feesys.printRecipe();}
	public double getTotalFee(){return feesys.getAllFee();}
	

}

class LotFactory{
	public static Lot genLot(int id, CAR_SIZE size){
		Lot lot = null;
		if(size.equals(CAR_SIZE.SMALL)){
			lot = new SmallLot(id);
		}
		else if(size.equals(CAR_SIZE.MEDIUM)){
			lot = new MediumLot(id);
		}
		else{
			lot = new BigLot(id);
		}
		return lot;
	}
}

abstract class Lot{
	private int id;
	private CAR_SIZE size;
	private boolean occupy;
	private Date checkin;
	private Date checkout;
	private Car car;
	public Lot(int id, CAR_SIZE size){
		this.id = id;
		this.size = size;
		this.checkin = null;
		this.checkout= null;
		occupy = false;
	}
	public CAR_SIZE getLotSize(){return size;}
	public boolean isFree(){return occupy == false;}
	public void checkin(Car car){
		this.car = car;
		occupy = true;
		checkin = new Date();
	}
	public Recipe checkout(double price){
		checkout = new Date();
		long diff = checkout.getTime()-checkin.getTime();
		diff = (long)(Math.ceil((double) diff/60/60/1000));
		double fee = price*diff;
		this.occupy = false;
		return new Recipe(checkin,checkout,car.getlicence(),car.getCarSize(),fee);
	}
	
	
}
class SmallLot extends Lot{
	public SmallLot(int id){
		super(id,CAR_SIZE.SMALL);
	}
}
class MediumLot extends Lot{
	public MediumLot(int id){
		super(id,CAR_SIZE.MEDIUM);
	}
}
class BigLot extends Lot{
	public BigLot(int id){
		super(id,CAR_SIZE.BIG);
	}
}
class FeeManagement{
	private List<Recipe> recipes;
	private double totalnum;
	public FeeManagement(){
		recipes = new ArrayList<>();
		totalnum = 0;
	}
	public void collectMoney(Recipe rec){
		recipes.add(rec);
		totalnum += rec.getfee();
	}
	public void printRecipe(){
		for(Recipe rec:recipes){
			System.out.println(rec.getRecipe());
		}
	}
	public double getAllFee(){return totalnum;}
}

class Recipe{
	private Date startdate;
	private Date endtime;
	private String licence;
	private CAR_SIZE size;
	private double fee;
	public Recipe(Date startdate, Date endtime, String licence, CAR_SIZE size, double fee){
		this.size = size;
		this.licence = licence;
		this.startdate = startdate;
		this.endtime = endtime;
		this.fee = fee;
	}
	public double getfee(){return fee;}
	public String getRecipe(){
		DateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append("Licence: ");
		sb.append(this.licence);
		sb.append(", Size: ");
		sb.append(this.size);
		sb.append(", Time:");
		sb.append(dataFormat.format(this.startdate));
		sb.append("-");
		sb.append(dataFormat.format(this.endtime));
		sb.append(", Total fee:");
		sb.append(this.fee);
		return sb.toString();
	}
}


abstract class Car{
	protected CAR_SIZE size;
	protected String licence;
	public Car(CAR_SIZE size,String licence){
		this.size = size;
		this.licence = licence;
	}
	public CAR_SIZE getCarSize(){return size;}
	public String getlicence(){return licence;}
}

class SmallCar extends Car{
	public SmallCar(String licence){
		super(CAR_SIZE.SMALL,licence);
	}
}

class MediumCar extends Car{
	public MediumCar(String licence){
		super(CAR_SIZE.MEDIUM,licence);
	}
}


class BigCar extends Car{
	public BigCar(String licence){
		super(CAR_SIZE.BIG,licence);
	}
}