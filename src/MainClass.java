import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Math;



public class MainClass {

    public static LinkedHashSet<String> attributes = new LinkedHashSet<>();
    public static LinkedHashMap<String, LinkedHashSet<String>> attributeValues = new LinkedHashMap<>();
    public static double E_S;

    public static LinkedHashMap<String, Integer> attributeColumnMap = new LinkedHashMap<>();
    public static LinkedHashMap<Integer, String> columnAttributeMap = new LinkedHashMap<>();
    public static String TARGET_ATTRIBUTE;
    public static LinkedHashSet<String> visited = new LinkedHashSet<>();



    public static double log_2(double value){
        return (Math.log(value)/Math.log(2));
    }


    public static String[][] loadWeatherDataSet(){

        String dataset[][] = {
                {"Temperature", "Outlook", "Humidity", "Windy", "Play Golf"},
                {"hot", "sunny", "high", "false", "no"},
                {"hot", "sunny", "high", "true", "no"},
                {"hot", "overcast", "high", "false", "yes"},
                {"cool", "rain", "normal", "false", "yes"},
                {"cool", "overcast", "normal", "true", "yes"},
                {"mild", "sunny", "high", "false", "no"},
                {"cool", "sunny", "normal", "false", "yes"},
                {"mild", "rain", "normal", "false", "yes"},
                {"mild", "sunny", "normal", "true", "yes"},
                {"mild", "overcast", "high", "true", "yes"},
                {"hot", "overcast", "normal", "false", "yes"},
                {"mild", "rain", "high", "true", "no"},
                {"cool", "rain", "normal", "true", "no"},
                {"mild", "rain", "high", "false", "yes"}
        };

        for(int i=0; i<dataset[0].length; i++){
            attributeColumnMap.put(dataset[0][i], i);
            columnAttributeMap.put(i, dataset[0][i]);
        }

        TARGET_ATTRIBUTE = dataset[0][dataset[0].length-1];

        return dataset;

    }


    public static void getAttributes(String dataset[][]){
        for(int i=0; i<dataset[0].length; i++){
            attributes.add(dataset[0][i]);
        }

    }

    public static void findAttributeValues(String dataset[][]) {
        for(String attribute: attributes){

            int column = attributeColumnMap.get(attribute);
            //System.out.println(column);
            LinkedHashSet<String> temp = new LinkedHashSet<>();
            for(int i=1; i<dataset.length; i++){
                temp.add(dataset[i][column]);
            }

            attributeValues.put(attribute, temp);

        }
    }

    public static LinkedHashMap<String, Integer> countAttributeValueAppearance(String dataset[][], String attribute){
        int column;
        column = attributeColumnMap.get(attribute);
        LinkedHashMap <String, Integer> count = new LinkedHashMap<>();
        int i=0;
        for(String[] row: dataset){

            if(!count.containsKey(row[column])){
                count.put(row[column], 1);
            }
            else{
                count.replace(row[column], count.get(row[column])+1);
            }

        }
        return count;
    }



    public static double entropySample(String dataset[][], String attribute){
        Object[] attributesArray = attributes.toArray();
        LinkedHashMap<String, Integer> count = countAttributeValueAppearance(dataset, attribute);

        double total=0;
        for(int i: count.values()){
            total+=i;
        }

        double entropy = 0;
        for(double value: count.values()){
            double division = value/total;
            entropy+= (division*log_2(division));
        }

        entropy *= -1;


        //System.out.println("Entropy of "+ attribute+": "+ entropy);
        return entropy;

    }

    public static String[][] splitDataSet(String[][] dataset, LinkedHashMap<String, String> conditions){
        ArrayList<String[]> newDataSet = new ArrayList<>();
        Boolean include;
        for(int i=0; i<dataset.length; i++){
            include = false;
            for(int j=0; j<dataset[0].length; j++){

                if(conditions.containsKey(columnAttributeMap.get(j))){

                    if(conditions.containsValue(dataset[i][j])){
                        include = true;
                    }

                }

            }
            if(include == true)
                newDataSet.add(dataset[i]);
        }

        String[][] new_dataset = null;
        if(newDataSet.size() > 0){
            new_dataset = new String[newDataSet.size()][newDataSet.get(0).length];


            for(int i=0; i<newDataSet.size(); i++){
                for(int j=0; j<newDataSet.get(0).length; j++){
                    new_dataset[i][j] = newDataSet.get(i)[j];
                }
            }

        }

        return new_dataset;
    }

    public static double entropyAgainstTarget(String dataset[][], String attribute){
        if(dataset == null || dataset.length == 0)
            return 0.0;
        double total = dataset.length;
        LinkedHashMap<String, Integer> count = new LinkedHashMap<>();

        for(int i=0; i<dataset.length; i++){
            for(int j=0; j<dataset[0].length; j++){

                if(j == attributeColumnMap.get(attribute)){
                    if(!count.containsKey(dataset[i][j])){
                        count.put(dataset[i][j], 1);
                    }
                    else{
                        count.replace(dataset[i][j], count.get(dataset[i][j])+1);
                    }
                }

            }
        }

        double entropy = 0;
        for(double value: count.values()){
            double division = value/total;
            entropy+= (division*log_2(division));
        }

        entropy *= -1;


        return entropy;
    }


    public static double entropyAttribute(String dataset[][], String attribute){
        LinkedHashSet<String> subset = attributeValues.get(attribute);
        Object[] subsetArray = subset.toArray();
        String temp_dataset[][];

        double entropy = 0;
        for(int i = 0; i<subsetArray.length; i++){
            LinkedHashMap<String, String> temp = new LinkedHashMap<>();
            String attributeValue = (String)subsetArray[i];
            temp.put(attribute, attributeValue);
            temp_dataset = splitDataSet(dataset, temp);
            double var1 = 0.0;
            if(temp_dataset != null)
                var1 = (double) temp_dataset.length/(double)(dataset.length);//changed here, removed -1 from dataset.length. becuase after removing head there is pure dataset only...

            double tempVar = var1*entropyAgainstTarget(temp_dataset, TARGET_ATTRIBUTE);
            entropy+=tempVar;

        }

        return entropy;
    }


    public static void constructTree(String[][] dataset, Node node, double entropy_parent, String edge_name){
        if(entropy_parent == -0.0)
        {
            node.is_leaf = true;
            node.name = dataset[0][attributeColumnMap.get(TARGET_ATTRIBUTE)];
            return;
        }


        LinkedHashMap<String, Double> entropyAttributes = new LinkedHashMap<>();
        for(Object attribute: attributes.toArray()){
            if((String)attribute == TARGET_ATTRIBUTE || visited.contains((String)attribute))
                continue;
            entropyAttributes.put((String)attribute, entropyAttribute(dataset, (String)attribute));
        }

        LinkedHashMap<String, Double> IGAttributes = new LinkedHashMap<>();
        Object[] entropyAttributesKeySet = entropyAttributes.keySet().toArray();
        Object[] entropyAttributesValueSet = entropyAttributes.values().toArray();
        for(int i=0; i<entropyAttributesKeySet.length; i++){
            IGAttributes.put((String)entropyAttributesKeySet[i], (entropy_parent - (Double)entropyAttributesValueSet[i]));
        }


        int max_index = 0;
        Object[] IGAttributesKeySet = IGAttributes.keySet().toArray();
        Object[] IGAttributesValueSet = IGAttributes.values().toArray();
        for(int i=1; i < IGAttributesValueSet.length; i++){
            if((double)IGAttributesValueSet[i] > (double)IGAttributesValueSet[max_index])
                max_index = i;
        }

        //System.out.println("Maximmum Information Gain: "+IGAttributesKeySet[max_index]+" with value of "+ IGAttributesValueSet[max_index]);

        node.name = (String)IGAttributesKeySet[max_index];
        node.edge_name = edge_name;
        node.entropy = entropyAttributes.get(node.name);
        node.IG = IGAttributes.get(node.name);



        Object[] attributevalues = attributeValues.get(IGAttributesKeySet[max_index]).toArray();
        for(int i=0; i<attributevalues.length; i++){
            Node temp = new Node();
            temp.edge_name = (String)attributevalues[i];
            node.links.add(temp);
            //Now here split the dataset according to the edge...
            LinkedHashMap<String, String> condition = new LinkedHashMap<>();
            condition.put((String)IGAttributesKeySet[max_index], temp.edge_name);
            String[][] splitedDataSet = splitDataSet(dataset, condition);

            double tempEntropy = entropySample(splitedDataSet, TARGET_ATTRIBUTE);

            //Here check if tempEntropy after split is zero do nothing just create nodes for all splits...

            visited.add(node.name);
            constructTree(splitedDataSet, temp, tempEntropy, temp.edge_name);


        }
    }

    public static void DecisionTree(String[][] dataset){

        getAttributes(dataset);
        findAttributeValues(dataset);


        dataset = removeHead(dataset);
        E_S = entropySample(dataset, TARGET_ATTRIBUTE);
        if(E_S == 0.0){
            System.out.println("Tree can't be constructed.");
            return;
        }
        //System.out.println("Entropy Sample: "+ E_S);

        Node root = new Node();
        constructTree(dataset, root, E_S, "");


        //Print Tree Entropy and IG...
        System.out.println("Tree: ");
        printTree(root, 1);
        System.out.println("");
        System.out.println("");



        //Rules Generation and Rules Print...
        ArrayList<String> rules = new ArrayList<>();
        String rule = "IF ";
        generateRules(root, rule, rules);
        printRules(rules);
        System.out.println("");
        System.out.println("");


        //Extracted Features...
        System.out.println("Extracted Features: ");
        int featureNumber = 1;
        for(Object feature: visited.toArray()){
            System.out.println(featureNumber+"- "+feature);
            featureNumber++;
        }

        System.out.println("");
        System.out.println("");


        //Process Input...
        System.out.println("Input Processing:");
        String[] input = {"cool", "sunny", "normal", "false", "?"};
        System.out.println("Before input processed.");
        printRow(input);
        processInput(root, input);
        System.out.println("After input processed.");
        printRow(input);

        System.out.println("");
        System.out.println("");

        System.out.println("Finished!");


    }

    public static void printRow(String[] row){
        System.out.print("[ ");
        int i=0;
        for(String value: row){

            if(i==row.length-1)
                System.out.print(value+" ");
            else
                System.out.print(value+", ");

            i++;
        }
        System.out.println("]");
    }

    public static  void generateRules(Node root, String rule, ArrayList<String> rules){
        rule += root.name;
        String temp_rule;
        for(Node lks: root.links){
            temp_rule = rule;
            if(!lks.is_leaf)
            temp_rule+="="+lks.edge_name+" AND ";
            else{
                temp_rule+="="+lks.edge_name+" THEN ["+TARGET_ATTRIBUTE+"]="+lks.name;
                rules.add(temp_rule);
                continue;
            }
            rule = temp_rule;
            generateRules(lks, rule, rules);
            rule ="IF "+root.name;
        }
    }

    public static void printTree(Node root, int level){
        String arrow = " --------------->";
        System.out.println(arrow+ " " +root.name);
        String temp = "";
        for(Node lks: root.links){

            for(int j=0; j<2; j++){
                temp="";
                for(int i=0; i<(arrow.length()+(root.name.length())/2)*level*2; i++){
                    temp+=" ";
                }
                temp+="|";
                System.out.println(temp);
            }

            temp="";
            for(int i=0; i<(arrow.length()+(root.name.length())/2)*level*2; i++){
                temp+=" ";
            }
            String line = "____________________";
            System.out.print(temp+line+" "+lks.edge_name);
            printTree(lks, level+1);

        }

    }


    public static void printRules(ArrayList<String> rules){
        System.out.println("");
        System.out.println("Rules:");
       Iterator<String> rulesIterator = rules.iterator();
       int i=1;
       while (rulesIterator.hasNext()){
           System.out.println("Rule "+i+": "+rulesIterator.next());
           i++;
       }
        System.out.println("");

    }

    public static  void processInput(Node root, String[] input){
        if(root.is_leaf)
        {
            input[attributeColumnMap.get(TARGET_ATTRIBUTE)] = root.name;
            return;
        }

        String value = root.name;
        for(Node link: root.links){
            if(link.edge_name == input[attributeColumnMap.get(root.name)]){
                processInput(link, input);
            }
        }
    }


    public static String[][] removeHead(String[][] dataset){
        String[][] mDataSet = new String[dataset.length-1][dataset[0].length];
        for(int i=0; i<dataset.length-1; i++){
            for(int j=0; j<dataset[0].length; j++){
                mDataSet[i][j] = dataset[i+1][j];
            }
        }

        return mDataSet;
        
    }



    public static void main(String[] args) {

        String dataset[][] = loadWeatherDataSet();
        DecisionTree(dataset);

        System.out.println("_________________________________");


        //Entropy: If the sample is completely homogeneous the entropy is zero and if the sample is an equally divided it has entropy of one.

    }

}
