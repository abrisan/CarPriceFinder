import static java.lang.System.out;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    int id;

    public Document setUp(String link){
        try{
            return Jsoup.connect(link).get();
        }catch (Exception e){
            return null;
        }
    }

    public List<Car> getCarsWithAttributesForDocument(Document doc){

        List<Car> ret = new ArrayList<Car>();

        Elements cars_no_prices = doc.getElementsByClass("offer-item__params");
        Elements cars_prices = doc.getElementsByClass("offer-price__number");

        for(int i = 0; i < cars_prices.size(); ++i){
            Elements details = cars_no_prices.get(i).getElementsByClass("offer-item__params-item");
            Element yr = details.get(0);
            int year = Integer.parseInt(yr.getElementsByTag("span").text());
            Element mls = details.get(1);
            String miles = mls.getElementsByTag("span").text().trim();
            int mileage = Integer.parseInt(miles.substring(0, miles.length()-2).trim().replaceAll("\\s+",""));
            Element price = cars_prices.get(i);
            String p = price.getElementsByTag("span").text().trim();
            int p1 = Integer.parseInt(p.substring(0, p.length()-7).trim().replaceAll("\\s+",""));
            Car c = new Car(this.id, p1, mileage, year);
            ++this.id;
            ret.add(c);
        }

        return ret;
    }

    public List<Car> getCarsFromBestAuto(Document doc){

        List<Car> ret = new ArrayList<>();

        Elements cars = doc.getElementsByClass("vehicle");


        for(Element car : cars){
            Elements info = car.getElementsByClass("vehicle_features").first().getElementsByTag("td");
            Elements price_container = car.getElementsByClass("vehicle_info");
            String pr = price_container.first().getElementsByTag("h3").first().getElementsByTag("span").text().trim();
            int price = Integer.parseInt(pr.substring(0, pr.length()-3).trim().replaceAll("\\s+|\\.",""));
            int yr = Integer.parseInt(info.first().text());
            String mls = info.last().text().trim().replaceAll("km","");
            int mileage = -1;
            if(mls.length()>0){
                mileage = Integer.parseInt(mls.replaceAll("\\s+|\\.",""));
            }
            Car c = new Car(this.id, price, mileage, yr);
            ret.add(c);
            ++this.id;
        }

        return ret;
    }

    public List<Car> getCarsFromMobile(Document doc){

        List<Car> ret = new ArrayList<>();

        Elements infos = doc.getElementsByClass("rbt-regMilPow");
        Elements prices = doc.getElementsByClass("price-block u-margin-bottom-9");

        if(infos.size() != prices.size()) return null;

        for(int i = 0 ; i < infos.size() ; ++i){
            String inf = infos.get(i).text();
            String[] info = inf.split(",");
            int year = -1;
            if(info[0].equals("Neuwagen")){
                year = 2016;
            }else{
                year = Integer.parseInt(info[0].split("/")[1]);
            }
            int mileage = Integer.parseInt
                    (info[1].substring(0, info[1].length()-2).trim().replaceAll("\\s+|\\.",""));
            String pr = prices.get(i).getElementsByClass("h3 u-block").first().text();
            int price = Integer.parseInt(pr.substring(0, pr.length()-1).trim().replaceAll("\\s+|\\.",""));
            Car c = new Car(this.id, price, mileage, year);
            ret.add(c);
        }

        return ret;
    }


    public double getMean(List<Car> arg, String value){
        double ret = 0;
        if(value.equals("miles")){
            for(Car c : arg) ret+=c.getMileage();
        }else if(value.equals("price")){
            for(Car c : arg) ret+=c.getPrice();
        }
        return ret/arg.size();
    }

    public int getMax(List<Car> arg, String value){
        int ret = 0;
        if(value.equals("miles")){
            for(Car c : arg) if(c.getMileage() > ret) ret = c.getMileage();
        }else if(value.equals("price")){
            for(Car c : arg) if(c.getPrice() > ret) ret = c.getPrice();
        }
        return ret;
    }

    public int getMin(List<Car> arg, String value){
        int ret = Integer.MAX_VALUE;
        if(value.equals("miles")){
            for(Car c : arg) if(c.getMileage() < ret) ret = c.getMileage();
        }else if(value.equals("price")){
            for(Car c : arg) if(c.getPrice() < ret) ret = c.getPrice();
        }
        return ret;
    }

    public double getDeviation(List<Car> arg, String value, double mean){
        double ret = 0;
        if(value.equals("miles")){
            for(Car c : arg) ret += Math.pow(c.getMileage()-mean, 2);
        }else if(value.equals("price")){
            for(Car c : arg) ret += Math.pow(c.getPrice()-mean, 2);
        }
        return Math.sqrt(ret/arg.size());
    }

    public List<Car> getCarsForStartingDocument(Main m, Document doc, String link_class, String website, String initLink){
        List<Car> cars = new ArrayList<>();
        List<String> pages = new ArrayList<>();
        if(website.equals("autovit")){
            if(doc != null){
                cars.addAll(m.getCarsWithAttributesForDocument(doc));
            }
        }else if(website.equals("bestauto")){
            if(doc != null){
                cars.addAll(m.getCarsFromBestAuto(doc));
            }
        }else if(website.equals("mobile")){
            if(doc != null){
                cars.addAll(getCarsFromMobile(doc));
             //   pages.addAll(m.getPagesForDocument(doc, link_class));
            }/*
            Iterator<String> pgs = pages.iterator();
            while(pgs.hasNext()){
                Document d = m.setUp(pgs.next());
                if(d!=null){
                    cars.addAll(m.getCarsWithAttributesForDocument(d));
                }
            }*/
        }

        return cars;
    }

    public static void main(String[] args){
        if(args.length < 3){
            System.out.println("Usage : java Main [year-of-prod] [min-miles] [max-miles]");
            return ;
        }
        Main m = new Main();
        Document autovit = m.setUp("https://www.autovit.ro/autoturisme/audi/q5/?search%5Bfilter_enum_damaged%5D=0&search%5Bcountry%5D=");
        Document bestauto = m.setUp("http://www.bestauto.ro/auto/audi/q5/pg1/0/?damagedcar=2");
        List<Car> cars = m.getCarsForStartingDocument(m, autovit, "om-pager rel", "autovit", null);
        out.println(cars.size());
        cars.addAll(m.getCarsForStartingDocument(m, bestauto, "pages", "bestauto", null));
        cars.addAll(m.getCarsForStartingDocument(m, null, null, "mobile", "http://suchen.mobile.de/fahrzeuge/search.html?isSearchRequest=true&scopeId=C&makeModelVariant1.makeId=1900&makeModelVariant1.modelId=32&maxPowerAsArray=PS&minPowerAsArray=PS&damageUnrepaired=NO_DAMAGE_UNREPAIRED"));
        out.println("GOT CARS");
        out.print("Sample Size is ");
        out.println(cars.size());
        int year = Integer.parseInt(args[0]);
        int minKm = Integer.parseInt(args[1]);
        int maxKm = Integer.parseInt(args[2]);
        try{
            PrintWriter output = new PrintWriter("output.txt","UTF-8");
            makeBanner(output, "START OF DOCUMENT");
            output.println();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            output.println(String.format("GENERATED ON : %s", date.toString()));
            output.println();
            output.println("Dataset obtained from Autovit and Bestauto and Mobile.de");
            output.println();
            makeBanner(output, "BEGIN RAW DATA STATISTICS");
            output.println(String.format("Min mileage: %d km", m.getMin(cars, "miles")));
            output.println(String.format("Max mileage: %d km", m.getMax(cars, "miles")));
            output.println();
            double meanMileage = m.getMean(cars, "miles");
            output.println(String.format("Mean mileage: %f km", meanMileage));
            output.println(String.format("Mileage Standard Deviation : %f km",
                    m.getDeviation(cars, "miles", meanMileage)));
            output.println();
            output.println(String.format("Min price %d EUR", m.getMin(cars, "price")));
            output.println(String.format("Max price %d EUR", m.getMax(cars, "price")));
            double meanPrice = m.getMean(cars, "price");
            output.println(String.format("Mean price %f EUR", meanPrice));
            output.println(String.format("Standard Deviation price %f EUR", m.getDeviation(cars, "price", meanPrice)));
            output.println();
            makeBanner(output, "END RAW DATA STATISTICS");
            output.println();
            makeBanner(output,"FILTERING CRITERIA");
            output.println();
            output.println(String.format("Year : %d", year));
            output.println(String.format("Mileage : %d - %d km", minKm, maxKm));
            output.println();
            makeBanner(output, "END FILTERING CRITERIA");
            List<Car> filtered = new ArrayList<Car>();
            for(Car c : cars){
                if(c.getYear() == year && c.getMileage() >= minKm && c.getMileage() <= maxKm){
                    filtered.add(c);
                }
            }
            for(Car c : filtered) output.println(c);
            output.println();
            makeBanner(output, "BEGIN FILTERED DATASET STATISTICS");
            output.println(String.format("Min mileage: %d km", m.getMin(filtered, "miles")));
            output.println(String.format("Max mileage: %d km", m.getMax(filtered, "miles")));
            output.println();
            double meanMileageF = m.getMean(filtered, "miles");
            output.println(String.format("Mean mileage: %f km", meanMileageF));
            output.println(String.format("Mileage Standard Deviation : %f km",
                    m.getDeviation(filtered, "miles", meanMileageF)));
            output.println();
            output.println(String.format("Min price %d EUR", m.getMin(filtered, "price")));
            output.println(String.format("Max price %d EUR", m.getMax(filtered, "price")));
            double meanPriceF = m.getMean(filtered, "price");
            output.println(String.format("Mean price %f EUR", meanPriceF));
            output.println(String.format("Standard Deviation price %f EUR",
                    m.getDeviation(filtered, "price", meanPriceF)));
            output.println();
            makeBanner(output, "END FILTERED DATASET STATISTICS");
            output.println();
            makeBanner(output, "END OF DOCUMENT");
            output.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void makeBanner(PrintWriter out, String text){
        out.println("    ===================");
        out.println(text);
        out.println("    ===================");
    }

}
