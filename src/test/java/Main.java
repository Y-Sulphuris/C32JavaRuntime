import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
		float[] f = new float[]{
				0.96f,
				0.38f,
				0.74f,
				0.38f,
				0.11f,
				0.11f,
				0.63f,
				0.49f,
				0.15f,
				0.21f,
				0.73f,
				0.87f,
				0.14f,
				0.68f,
				0.96f,
				0.19f,
				0.34f,
				0.28f,
				0.54f,
				0.68f,
				0.36f,
				0.82f,
				0.89f,
				0.47f,
				0.29f,
				0.36f,
				0.67f,
				0.64f};

		Arrays.sort(f);
		for (int i = 0; i < f.length; i++) {
			System.out.println(f[i]);
		}
	}
}
