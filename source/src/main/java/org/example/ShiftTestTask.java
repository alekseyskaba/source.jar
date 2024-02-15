package org.example;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ShiftTestTask {

    //Списки для хранения разных типов данных из входных файлов.
    private final List<String> stringList = new ArrayList<>();
    private final List<Long> integerList = new ArrayList<>();
    private final List<Double> doubleList = new ArrayList<>();

    //Список входных файлов.
    List<String> fileNameList = new ArrayList<>();

    //Директория, в которой располагается утилита
    private String currentPath;

    //Базовые настройки утилиты.
    private String resultPath = null;
    private boolean updateFile = false;
    private boolean fullStatistic = false;
    String resultFileNamePrefix = "";


    private boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage() + " при проверке имени файла.");
            return false;
        }
    }

    public <T> void writeInOutputFile(List<T> strings, String fileName) {
        try (BufferedWriter bufferedWriter =
                     new BufferedWriter(
                             new FileWriter(resultPath + resultFileNamePrefix + fileName, updateFile)
                     )
        ){
            for(T s:strings)
                bufferedWriter.write(s.toString() + "\n");
            System.out.println(String.format("Количество записей в файле %s: %d", resultFileNamePrefix + fileName, strings.size()));
        }
        catch (IOException e) {
            System.out.println("Ошибка " + e.getMessage() + " при записи в файл.");
        }
    }

    private String readFromInputStream(InputStream inputStream) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e){
            System.out.println("Ошибка чтения из файла.");
        }
        return resultStringBuilder.toString();
    }

    public void run(String[] args) {
        //Присваиваем текущее местоположение для создания выходных файлов.
        try {
            currentPath = ShiftTestTask.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            int index = currentPath.lastIndexOf("/");
            currentPath = currentPath.substring(0, index + 1);
        } catch (URISyntaxException e) {
            System.out.println("Ошибка: " + e.getMessage() + " .Запустите утилиту повторно.");
            return;
        }

        //Читаем и анализируем опции для выполнения.
        boolean flagStat = false;
        boolean flagInput = false;
        boolean flagResult = false;
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-o")) {
                if (!flagResult) {
                    resultPath = args[i + 1];
                    flagResult = true;
                } else {
                    System.out.println("Предупреждение: повторный ввод пути для выходных файлов. Будет использован первый введённый путь.");
                }
            }
            if(args[i].equals("-p")) {
                if (!flagInput) {
                    resultFileNamePrefix = args[i + 1];
                    flagInput = true;
                } else {
                    System.out.println("Предупреждение: повторный ввод префикса для названий выходных файлов. Будет использован первый введённый префикс.");
                }
            }
            if(args[i].equals("-a"))
                updateFile = true;
            if(args[i].equals("-s")) {
                if(!flagStat) {
                    fullStatistic = false;
                    flagStat = true;
                } else{
                    System.out.println("Предупреждение: повторный ввод опции для статистики. Будет использована первая переменная.");
                }
            }
            if(args[i].equals("-f")) {
                if(!flagStat) {
                    fullStatistic = true;
                    flagStat = true;
                } else{
                    System.out.println("Предупреждение: повторный ввод опции для статистики. Будет использована первая переменная.");
                }
            }
            if(args[i].endsWith(".txt") && isFilenameValid(args[i]))
                fileNameList.add(args[i]);
        }

        //Проверяем, есть ли входные файлы.
        if(fileNameList.isEmpty()) {
            System.out.println("Не обнаружены входные файлы. Утилиту необходимо запустить повторно с указанием входных файлов.");
            return;
        }

        //Проверяем префикс для выходных файлов на корректность.
        if(!isFilenameValid(resultFileNamePrefix + "testname.txt")) {
            System.out.println("Для выходных файлов будут использоваться стандартные названия.");
            resultFileNamePrefix = "";
        }

        //Проверяем входную переменную для создания файлов.
        if(resultPath == null)
            resultPath = currentPath;
        else{
            File file = new File(resultPath);
            if(!Files.exists(Paths.get(resultPath)) || !file.isDirectory()) {
                System.out.println("Введён некорректный путь для выходных файлов. Файлы будут располагаться в директории с утилитой.");
                resultPath = currentPath;
            } else{
                resultPath = file.getPath() + "\\";
            }
        }

        //Создаём общий массив строк из всех файлов.
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for(String s:fileNameList){
                stringBuilder.append(readFromInputStream(new FileInputStream(currentPath + s)));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ошибка при чтении из файла: " + e.getMessage());
        }

        String[] strings = stringBuilder.toString().split("\n");

        //Распределяем строки по отдельных спискам.
        for (int i = 0; i < strings.length; i++) {
            if(isLong(strings[i])){
                integerList.add(Long.parseLong(strings[i]));
            } else if (isDouble(strings[i])) {
                doubleList.add(Double.parseDouble(strings[i]));
            } else{
                stringList.add(strings[i]);
            }
        }

        //Запись в файлы
        if(!stringList.isEmpty()) writeInOutputFile(stringList, "strings.txt");
        if(!integerList.isEmpty()) writeInOutputFile(integerList, "integers.txt");
        if(!doubleList.isEmpty()) writeInOutputFile(doubleList, "floats.txt");

        //Вывод полной статистики, если она необходима.
        if(fullStatistic){
            if(!integerList.isEmpty()) {
                System.out.println("Максимальное целое число: " + integerList
                        .stream()
                        .max(Long::compare)
                        .get()
                );
                System.out.println("Минимальное целое число: " + integerList
                        .stream()
                        .min(Long::compare)
                        .get()
                );
                long sum = 0;
                for (int i = 0; i < integerList.size(); i++) {
                    sum += integerList.get(i).longValue();
                }
                System.out.println("Сумма целых чисел: " + sum);
                System.out.println("Среднее значение целых чисел: " + (double)sum/integerList.size());
            }
            if(!doubleList.isEmpty()) {
                System.out.println("Максимальное вещественное число: " + doubleList
                        .stream()
                        .max(Double::compare)
                        .get()
                );
                System.out.println("Минимальное вещественное число: " + doubleList
                        .stream()
                        .min(Double::compare)
                        .get()
                );
                double sum = 0.0;
                for (int i = 0; i < doubleList.size(); i++) {
                    sum += doubleList.get(i).doubleValue();
                }
                System.out.println("Сумма вещественных чисел: " + sum);
                System.out.println("Среднее значение вещественных чисел: " + sum/doubleList.size());
            }
            if(!stringList.isEmpty()){
                System.out.println("Длина самой длинной строки: " + stringList
                        .stream()
                        .max(Comparator.comparing(a -> a.length()))
                        .get()
                        .length()
                );
                System.out.println("Длина самой короткой строки: " + stringList
                        .stream()
                        .min(Comparator.comparing(a -> a.length()))
                        .get()
                        .length()
                );
            }
        }

    }

    public static void main(String[] args) {
        new ShiftTestTask().run(args);
    }
}