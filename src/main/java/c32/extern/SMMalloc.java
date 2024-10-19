package c32.extern;

// ух нихрена себе
public abstract class SMMalloc {
	private SMMalloc() {}


	public static final class CacheWarmupOptions { public static final int
			CACHE_COLD = 0, // none tls buckets are filled from centralized storage
			CACHE_WARM = 1, // half tls buckets are filled from centralized storage
			CACHE_HOT = 2;  // all tls buckets are filled from centralized storage

		private CacheWarmupOptions() throws InstantiationException
		{ throw new InstantiationException(); }
	};

	//public static final long main_sm_allocator = _sm_allocator_create(8,1024 * 1024 * 8);


	/**
	 * Create smmalloc allocator instance
	 *
	 * @param bucketsCount        the number of buckets in the allocator
	 * @param bucketSizeInBytes   the size of each bucket in bytes
	 * @return a native pointer to the created shared memory allocator
	 */
	public static native long _sm_allocator_create(int bucketsCount, long bucketSizeInBytes);

	/**
	 * Destroy smmalloc allocator instance
	 * @param space allocator which should be destroyed
	 */
	public static native void _sm_allocator_destroy(long space);

	/**
	 * Create thread cache for current thread
	 * @param space smmalloc allocator instance
	 */
	public static native void _sm_allocator_thread_cache_create(long space, int warmupOptions, int[] options);

	/**
	 * Create thread cache for current thread
	 * @param space smmalloc allocator instance
	 */
	public static native void _sm_allocator_thread_cache_create(long space, int warmupOptions, int cache_size);

	/**
	 * Destroy thread cache for current thread
	 * @param space smmalloc allocator which thread cache created for
	 */
	public static native void _sm_allocator_thread_cache_destroy(long space);

	/**
	 * Allocate aligned memory block
	 * @param space smmalloc allocator instance
	 * @param bytesCount size of memory block
	 * @param alignment alignment of memory block
	 * @return pointer to allocated memory block
	 */
	public static native long _sm_malloc(long space, long bytesCount, long alignment);

	/**
	 * Free memory block
	 * @param space smmalloc allocator which contains a memory block
	 * @param ptr pointer to memory block that should be freed
	 */
	public static native void _sm_free(long space, long ptr);

	/**
	 * Reallocate memory block
	 * @param space smmalloc allocator which contains a memory block
	 * @param ptr pointer to memory block that should be reallocated
	 * @param bytesCount size of new memory block
	 * @param alignment alignment of new memory block
	 * @return pointer to reallocated memory block
	 */
	public static native long _sm_realloc(long space, long ptr, long bytesCount, long alignment);

	/**
	 * Get usable memory size of given smmalloc allocator
	 * @param space smmalloc allocator instance
	 * @param ptr pointer
	 * @return usable memory size of given smmalloc allocator
	 */
	public static native long _sm_msize(long space, long ptr);

	/**
	 * @param space smmalloc allocator instance
	 * @param ptr pointer to memory in allocator space
	 * @return smmalloc bucket index of given ptr
	 */
	public static native int _sm_mbucket(long space, long ptr);

	/**
	 * @param space smmalloc allocator instance
	 * @return the number of buckets in allocator
	 */
	public static native long _sm_buckets_count(long space);
	
	/**
	 * Get the number of elements in the given smmalloc bucket
	 * @param space smmalloc allocator instance
	 * @param bucketIndex smmalloc bucket index
	 * @return the number of elements in given bucket
	 */
	public static native int _sm_buckets_element_count(long space, long bucketIndex);
	/**
	 * Check if given pointer is allocated by smmalloc allocator
	 * @param space smmalloc allocator instance
	 * @param ptr pointer to check
	 * @return true if given ptr is allocated by smmalloc allocator, false otherwise
	 */
	public static native boolean _sm_is_my_alloc(long space, long ptr);

}
