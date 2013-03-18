#include<stdio.h>
#include<stdlib.h>

unsigned int number_of_bits(unsigned int v)
{
	// c accumulates the total bits set in v
	unsigned int c;
	for (c = 0; v; c++)
	{
		// clear the least significant bit set
		v &= v - 1;
	}
	return c;
}

double jaccard_distance(unsigned int a, unsigned int b)
{
	printf("intersection: %u\n", number_of_bits(a & b));
	printf("union: %u\n", number_of_bits(a | b));
	return 1.0 - 1.0 * number_of_bits(a & b) / number_of_bits(a | b);
}

double jaccard_similarity(unsigned int a, unsigned int b)
{
	return 1.0 * number_of_bits(a & b) / number_of_bits(a | b);
}

int main(int argc, char **argv)
{
	unsigned int p1, p2;
	p1 = strtoul(argv[1], NULL, 10);
	p2 = strtoul(argv[2], NULL, 10);

	double distance = jaccard_distance(p1, p2);
	printf("Jaccard Similarity: %f\n", jaccard_similarity(p1, p2));
	printf("Jaccard Distance: %f\n\n", distance);
}
