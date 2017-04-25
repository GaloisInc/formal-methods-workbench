#include <smaccm_sender.h>
#include <stdio.h>
#include <inttypes.h>

void periodic_ping(const int64_t *periodic_100_ms) {

   printf("sender: periodic dispatch received (%lld).  Writing to receiver \n", *periodic_100_ms);
   
   test9__a_array_impl test_data;
   test_data[0] = 0;
   test_data[1] = 1;
   test_data[2] = 2;
   test_data[3] = 3;
   printf("sender: sending test data: (%d, %d, %d, %d) to receiver \n", test_data[0], test_data[1], test_data[2], test_data[3]);
   
   bool result = ping_Output1(test_data);
   printf("first attempt at pinging receiver was: %d. \n", result); 

   result = ping_Output1(test_data);
   printf("second attempt at pinging receiver was: %d. \n", result); 

}
