#-------------------------------------------------------------------------------
# Name:        module1
# Purpose:
#
# Author:      Tyler
#
# Created:     18/11/2016
# Copyright:   (c) Tyler 2016
# Licence:     <your licence>
#-------------------------------------------------------------------------------

import math

points = [
(255, 102),
(106, 52),
(68, 29),
(45, 16),
(242, 91),
(233, 87),
(224, 82),
(101, 42),
(104, 44),
(254,92),
(53,24),
(46,21),
]

def main():
    #Find the averages
    avgx = 0
    for p in points:
        avgx += p[0]
    avgx /= len(points)

    avgy = 0
    for p in points:
        avgy += p[1]
    avgy /= len(points)

    #Calculate Sxy
    sxy = 0
    for p in points:
        xd = p[0] - avgx
        yd = p[1] - avgy
        sxy += xd*yd

    #Calculate Sxx
    sxx = 0
    for p in points:
        xd = p[0] - avgx
        sxx += xd**2

    #Calculate Syy
    syy = 0
    for p in points:
        yd = p[1] - avgy
        syy += yd**2

    #Finally, calculate beta1
    beta1 = float(sxy) / float(sxx)

    #Then calculate beta0
    beta0 = avgy - avgx*beta1

    print("Y = " + str(beta1) + "*X + " + str(beta0))
    r = sxy / (math.sqrt(sxx) * math.sqrt(syy))
    print("and the r value is: " + str(r))


if __name__ == '__main__':
    main()
