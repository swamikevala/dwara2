package org.ishafoundation.dwaraapi.process.factory;

import java.util.HashMap;
import java.util.Map;

import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.ITranscoder;
import org.springframework.context.ApplicationContext;

public class TranscoderFactory {
	// https://medium.com/@kousiknath/design-patterns-different-approaches-to-use-factory-pattern-to-choose-objects-dynamically-at-run-71449bceecef
    private static final Map<String, Class<? extends ITranscoder>> instances = new HashMap<>();

    public static void register(String processName, Class<? extends ITranscoder> instance) {
        if (processName != null && instance != null) {
            instances.put(processName, instance);
        }
    }

    public static ITranscoder getInstance(ApplicationContext context, String processName) {
        if (instances.containsKey(processName)) {
            return context.getBean(instances.get(processName));
        }
        return null;
    }
}