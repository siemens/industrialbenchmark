from industrial_benchmark_python.IBGym import IBGym
import numpy as np

DISCOUNT = 0.97

env = IBGym(70)
env.reset()
returns = []
for _ in range(100):
    acc_return = 0.
    for i in range(100):
        state, reward, done, info = env.step(env.action_space.sample())
        acc_return += reward * DISCOUNT**i
    returns.append(acc_return / 100.)

print("random actions achieved return", np.mean(returns), "+-", np.std(returns))