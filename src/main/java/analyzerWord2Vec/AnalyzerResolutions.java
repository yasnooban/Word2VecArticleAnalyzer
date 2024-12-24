package analyzerWord2Vec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static analyzerWord2Vec.OperationForAnalyzedData.createDFForAnalysis;

public class AnalyzerResolutions {
    //final int a =
    static String filePathModel =
            "src/main/java/analyzerWord2Vec/pathToWriteto2.txt";

    public static void main(String[] args) throws Exception {
        startAnalyzer();
    }

    public static void startAnalyzer() throws Exception {
        String sentence = "относительно компонентов таблица имеет ошибки";
        // todo: может быть не одно решение
        String filePathLaw = Paths.get("src/main/java/analyzerWord2Vec/law.txt").toString();
        String filePathAstr = Paths.get("src/main/java/analyzerWord2Vec/astronomy.txt").toString();
        System.out.println(compare2(filePathAstr, sentence));;

        String sentence1 = "относительно компонентов таблица имеет ошибки";
        String sentence2 = "относительно имеет ошибки";
        compareTwo(sentence1,sentence2);

    }

    public static double compare2(String filePathModel, String sentence) throws IOException {
        double resultCompare;

        List<String> wordsList = createDFForAnalysis(filePathModel);
        Word2Vec word2Vec = createModel(createDataForLearnModel(wordsList));
        Collection<String> wordsInModel = word2Vec.vocab().words();
        resultCompare = comparisons2(word2Vec, sentence, wordsInModel);

        return resultCompare;
    }

    //получаем значения c текстом для обучения и обучаем модель
    private static Word2Vec createModel(Collection<String> textForLearn)  {

        SentenceIterator iter = new CollectionSentenceIterator(textForLearn);
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        // Обучение модели
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(5)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        vec.fit();

        // Запись векторов в файл
        try {
            WordVectorSerializer.writeWordVectors(vec, filePathModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return vec;
    }

    // Сравнение и вывод элемента с наиболее большим коэф совпадения
    private static double compare(Word2Vec word2Vec, String sentence1, String sentence2) throws IOException {

        // Загружаем модель
        WordVectors vecLoad = word2Vec;

        // Разделим предложения на слова
        String[] words1 = sentence1.split(" ");
        String[] words2 = sentence2.split(" ");

        // Вычислим средний вектор для каждого предложения
        INDArray vec1 = getAverageVector(words1, vecLoad);
        INDArray vec2 = getAverageVector(words2, vecLoad);

        // Вычислим косинусное сходство между векторами
        return cosineSimilarity(Objects.requireNonNull(vec1), vec2);
    }
    private static double comparisons2(Word2Vec word2Vec,String sentence1,
                                       Collection<String> sentence2) throws IOException {

        // Загружаем модель
        WordVectors vecLoad = word2Vec;

        // Разделим предложения на слова
        String[] words1 = sentence1.split(" ");

        String[] words2 = sentence2.toArray(new String[0]);
        System.out.println(words2[0] + "555");
        // Вычислим средний вектор для каждого предложения
        INDArray vec1 = getAverageVector(words1, vecLoad);
        INDArray vec2 = getAverageVector(words2, vecLoad);

        // todo: может быть не одно решение, сделать аналитический круг или диаграмму
        // Вычислим косинусное сходство между векторами
        return cosineSimilarity(Objects.requireNonNull(vec1), vec2);
    }

    // Вычислим косинусное сходство между векторами
    public static INDArray getAverageVector(String[] words, WordVectors wordVectors) {
        System.out.println(words.length+"111");

        INDArray totalVector =
                Nd4j.zeros(
                        wordVectors.getWordVectorMatrix(words[0])
                                .length());
        int numWords = 0;

        for (String word : words) {
            if (wordVectors.hasWord(word)) {
                totalVector.addi(
                        wordVectors.getWordVectorMatrix(word));
                numWords++;
            }
        }

        if (numWords > 0) {
            return totalVector.divi(numWords);
        } else {
            return null;
        }
    }

    // Вычислим косинусное сходство между векторами
    public static double cosineSimilarity(INDArray vec1, INDArray vec2) {
        double dotProduct = vec1.mul(vec2).sumNumber().doubleValue();
        double norm1 = vec1.norm2Number().doubleValue();
        double norm2 = vec2.norm2Number().doubleValue();

        if (norm1 > 0 && norm2 > 0) {
            return dotProduct / (norm1 * norm2);
        } else {
            return 0.0;
        }
    }

    public static double cosineSimilarity2(INDArray vec1, INDArray vec2) {
        double dotProduct = vec1.mul(vec2).sumNumber().doubleValue();
        double norm1 = vec1.norm2Number().doubleValue();
        double norm2 = vec2.norm2Number().doubleValue();

        if (norm1 > 0 && norm2 > 0) {
            return dotProduct / (norm1 * norm2);
        } else {
            return 0.0;
        }
    }

    // получаем из df текст для обучения
    public static Collection<String> createDataForLearnModel(List <String> dataFrame) {
        Collection<String> textForLearn = new ArrayList<>();
        //System.out.println(dataFrame.col(21));
        for (Object word : dataFrame) {
            if (word != null) {
                textForLearn.add(word.toString().toLowerCase().replaceAll(",", ""));
                System.out.println(word);
            }
        }
        return textForLearn;
    }
    // сравнение двух элементов
    public static void compareTwo(String sentence1, String sentence2) throws IOException {

        List<String> wordsList = createDFForAnalysis(filePathModel);
        Word2Vec word2Vec = createModel(createDataForLearnModel(wordsList));

        double comparisonsRes = compare(word2Vec, sentence1, sentence2);
        System.out.println(comparisonsRes);
    }
}







