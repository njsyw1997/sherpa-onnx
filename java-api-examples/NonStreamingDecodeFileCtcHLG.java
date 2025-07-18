// Copyright 2024 Xiaomi Corporation

// This file shows how to use an online CTC model, i.e., streaming CTC model,
// to decode files.
import com.k2fsa.sherpa.onnx.*;

public class NonStreamingDecodeFileCtcHLG {
  public static void main(String[] args) {
    String model =
        "./NonStreamingModel/model.int8.onnx";
    String tokens = "./NonStreamingModel/tokens_new.txt";
    String hlg = "./NonStreamingModel/HP.fst";
    String waveFilename = "./NonStreamingModel/test_wavs/1.wav";

    WaveReader reader = new WaveReader(waveFilename);

    OfflineZipformerCtcModelConfig ctc =
        OfflineZipformerCtcModelConfig.builder().setModel(model).build();

    OfflineModelConfig modelConfig =
        OfflineModelConfig.builder()
            .setZipformerCtc(ctc)
            .setTokens(tokens)
            .setNumThreads(1)
            .setDebug(true)
            .build();

    OfflineCtcFstDecoderConfig ctcFstDecoderConfig =
        OfflineCtcFstDecoderConfig.builder().setGraph(hlg).build();

    OfflineRecognizerConfig config =
        OfflineRecognizerConfig.builder()
            .setOfflineModelConfig(modelConfig)
            .setCtcFstDecoderConfig(ctcFstDecoderConfig)
            .build();

    OfflineRecognizer recognizer = new OfflineRecognizer(config);
    OfflineStream stream = recognizer.createStream();
    stream.acceptWaveform(reader.getSamples(), reader.getSampleRate());

    float[] tailPaddings = new float[(int) (0.3 * reader.getSampleRate())];
    stream.acceptWaveform(tailPaddings, reader.getSampleRate());

    recognizer.decode(stream);
    

    String text = recognizer.getResult(stream).getText();

    System.out.printf("filename:%s\nresult:%s\n", waveFilename, text);

    stream.release();
    recognizer.release();
  }
}
