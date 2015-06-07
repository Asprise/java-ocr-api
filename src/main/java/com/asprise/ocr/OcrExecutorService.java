/**********************************************************************************************
 *
 * Asprise OCR Java API
 * Copyright (C) 1998-2015. Asprise Inc. <asprise.com>
 *
 * This file is licensed under the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * You should have received a copy of the GNU Affero General Public License.  If not, please
 * visit <http://www.gnu.org/licenses/agpl-3.0.html>.
 *
 **********************************************************************************************/
package com.asprise.ocr;

import java.io.File;
import java.util.concurrent.*;

/**
 * Executor service for OCR.
 */
public class OcrExecutorService extends ThreadPoolExecutor {
    public static final ThreadLocal<Ocr> threadLocalOcr = new ThreadLocal<Ocr>();

    ConcurrentHashMap<Thread, Ocr> threadOcrMap = new ConcurrentHashMap<Thread, Ocr>();

    String lang;
    String speed;

    /**
     * A service executor with thread count equal to number of CPU core available.
     * @param lang language to recognize
     * @param speed speed settings, e.g., {@linkplain Ocr#SPEED_FASTEST}.
     */
    public OcrExecutorService(String lang, String speed) {
        this(lang, speed, getCpuCores());
    }

    /**
     * Creates a new instance of OCR service executor
     * @param lang language to recognize
     * @param speed speed settings, e.g., {@linkplain Ocr#SPEED_FASTEST}.
     * @param poolSize number of threads to be used in this executor.
     */
    public OcrExecutorService(String lang, String speed, int poolSize) {
        super(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
        this.lang = lang;
        this.speed = speed;
        Ocr.setUp();
    }

    private Ocr getOcr(Thread t) {
        Ocr ocr = threadOcrMap.get(t);
        if(ocr == null) {
            ocr = new Ocr();
            ocr.startEngine(lang, speed);
            //System.out.println("Engine started by thread " + Thread.currentThread());
            //ocr.stopEngine();
            //System.out.println("Stopped now.");
            threadOcrMap.put(t, ocr);
        }

        return ocr;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if(threadLocalOcr.get() == null) {
            threadLocalOcr.set(getOcr(t));
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        //System.out.println("Ocr finished.");
    }

    @Override
    protected void terminated() {
        super.terminated();
        // Can not stop here as the JVM will crash silently without any error message.
        // stopOcrEngines();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        stopOcrEngines();
    }

    public void stopOcrEngines() {
        // stop all OCR engines
        for(Ocr ocr : threadOcrMap.values()) {
            // System.out.println("Stopping engine by thread " + Thread.currentThread());
            ocr.stopEngine();
        }

        //System.out.println("All stopped.");
    }

    /**
     * Represents an OCR task.
     */
    public static class OcrCallable implements Callable<String> {
        File[] files;
        String recognizeType;
        String outputFormat;
        Object[] propSpec;

        String filesString;
        int pageIndex;
        int startX;
        int startY;
        int width;
        int height;

        /**
         * See {@linkplain com.asprise.ocr.Ocr#recognize(java.io.File[], String, String, Object...)}
         * @param files
         * @param recognizeType
         * @param outputFormat
         * @param propSpec
         */
        public OcrCallable(File[] files, String recognizeType, String outputFormat, Object... propSpec) {
            this.files = files;
            this.recognizeType = recognizeType;
            this.outputFormat = outputFormat;
            this.propSpec = propSpec;
        }

        /**
         * See {@linkplain com.asprise.ocr.Ocr#recognize(String, int, int, int, int, int, String, String, Object...)}
         * @param files
         * @param pageIndex
         * @param startX
         * @param startY
         * @param width
         * @param height
         * @param recognizeType
         * @param outputFormat
         * @param propSpec
         */
        public OcrCallable(String files, int pageIndex, int startX, int startY, int width, int height, String recognizeType, String outputFormat, Object... propSpec) {
            this.filesString = files;
            this.pageIndex = pageIndex;
            this.startX = startX;
            this.startY = startY;
            this.width = width;
            this.height = height;
            this.recognizeType = recognizeType;
            this.outputFormat = outputFormat;
            this.propSpec = propSpec;
        }

        @Override
        public String call() throws Exception {
            Ocr ocr = threadLocalOcr.get();
            if(ocr == null) {
                throw new RuntimeException("Internal error. Ocr is not found in thread local.");
            }

            if(files != null) {
                return ocr.recognize(files, recognizeType, outputFormat, propSpec);
            } else {
                return ocr.recognize(filesString, pageIndex, startX, startY, width, height, recognizeType, outputFormat, propSpec);
            }
        }
    }

    /**
     * Returns number of CPU core available for the current JVM.
     * If the CPU support hyper-threading, this number will be the double of the total cores.
     * @return
     */
    public static int getCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }
}
