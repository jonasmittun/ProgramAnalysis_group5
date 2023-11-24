# from https://thedatascientist.com/how-to-do-a-t-test-in-python/
from scipy.stats import ttest_rel

sign = [1,0,0,1,0,0,0,1,1,1,1,1,1,1,1]
hybrid = [1,1,1,1,1,1,0,1,1,1,1,1,1,1,1]
tests = [False,False,False,False,False,False,False,True,True,True,True,True,True,True,True]

t_stat, p_value = ttest_rel(sign, hybrid)
print("T-statistic value: ", t_stat)
print("P-Value: ", p_value)

sign_fp = 0
hy_fp = 0
sign_fn = 0
hy_fn = 0
for i in range(len(sign)):
    if sign[i] == 0:
        if tests[i]:
            sign_fp += 1
        else:
            sign_fn += 1
    if hybrid[i] == 0:
        if tests[i]:
            hy_fp += 1
        else:
            hy_fn += 1

test_pos = list.count(tests, True)
test_neg = list.count(tests, False)

sign_tp = (len(sign) - sign_fn - sign_fp)/len(tests)
hy_tp = (len(hybrid) - hy_fn - hy_fp)/len(tests)

print("Sign False Positives: ", 100*sign_fp/test_pos, "%")
print("Sign False Negatives: ", 100*sign_fn/test_neg, "%")
print("Sign True Positive: ", 100*sign_tp, "%")
print("Hybrid False Positives: ", 100*hy_fp/test_pos, "%")
print("Hybrid False Negatives: ", 100*hy_fn/test_neg, "%")
print("Hybrid True Positive: ", 100*hy_tp, "%")

sign_recall = sign_tp/(sign_tp + sign_fp)
hy_recall = hy_tp/(hy_tp + hy_fp)

sign_acc = sign_tp/(sign_tp + sign_fn)
hy_acc = hy_tp/(hy_tp + hy_fn)

sign_fscore = 2 * ((sign_recall*sign_acc)/(sign_recall + sign_acc))
hy_fscore = 2 * ((hy_recall*hy_acc)/(hy_recall + hy_acc))

print("Sign recall: ", sign_recall)
print("Sign precision: ", sign_acc)
print("Sign f-score: ", sign_fscore)
print("Hybrid recall: ", hy_recall)
print("Hybrid precision: ", hy_acc)
print("Hybrid f-score: ", hy_fscore)
