public class SimpleTesting {
    public static void main(String[] args){
        String str = "String - is a squence of chars:~!@#$%^&*()'. Test.";
        System.out.println(str);
        String result = str.replaceAll("\\p{Punct}", "");
        System.out.println(result);
    }
}