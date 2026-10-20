import benchmark.BenchmarkRunner;
import tests.TestSuite;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   Smart City Infrastructure & Route Planning System       ");
        System.out.println("==========================================================");

        System.out.println("\n[1/2] Running Correctness Verification Tests...");
        TestSuite.runAllTests();

        System.out.println("\n[2/2] Running Experimental Benchmarks...");
        BenchmarkRunner.runAllBenchmarks();

        System.out.println("\n==========================================================");
        System.out.println("All tasks finished successfully.");
        System.out.println("==========================================================");
    }
}
