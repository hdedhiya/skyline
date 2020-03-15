import matplotlib.pyplot as plt
import numpy as np

skyline = open('../../../target/skyline/skyline-1.txt', 'r')
points = open('../java/db/dataset1.txt', 'r')
modpoint = [34.78, 19.24] # Point inserted / deleted

p = []
for line in points:
    if len(line) != 0:
        tokens = line.strip().split(' ')
        if len(tokens) == 2:
            p.append((float(tokens[0]), float(tokens[1])))

points.close()

s = []
for line in skyline:
    if len(line) != 0:
        tokens = line.strip().split(' ')
        if len(tokens) == 2:
            s.append((float(tokens[0]), float(tokens[1])))

skyline.close()

# p = np.append(np.array(p), [modpoint], axis=0) # For insertion
p = np.array(p)
s = np.array(s)

plt.title("Deletion of (34.78, 19.24) Causing Change in Skyline")
plt.scatter(p[:,0], p[:,1])
plt.scatter(s[:,0], s[:,1], c='r')
plt.scatter(modpoint[0], modpoint[1], c='y', marker='x') # Show modified point
plt.show()
plt.close()