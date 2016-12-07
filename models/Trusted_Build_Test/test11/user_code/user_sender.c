#ifdef __TB_OS_CAMKES__
	#include <sender.h>
	#include <inttypes.h>
#elif __TB_OS_ECHRONOS__
	#include <debug.h>
#endif
#include "tb_sender.h"
#include <inttypes.h>
#ifndef __TB_OS_ECHRONOS
#include <stdio.h>
#endif

void sender_periodic_ping(const int64_t * periodic_100_ms) {

   printf("sender: periodic dispatch received (%d).  Writing to receiver \n", (int32_t)*periodic_100_ms);
   
   test11__a_struct_impl test_data;
   test_data.field1 = 1.0;
   test_data.field2 = 2.0;
   
   printf("sender: sending test data: (%f, %f) to receiver \n", test_data.field1, test_data.field2);
   
   bool result = ping_Output1(&test_data);
   printf("first attempt at pinging receiver was: %d. \n", result); 

   test_data.field1 = 3.0;
   test_data.field2 = 4.0;
   result = ping_Output1(&test_data);
   printf("second attempt at pinging receiver was: %d. \n", result); 

}
