
var module = angular.module('devhub', ['ui.bootstrap', 'xeditable', 'ui.bootstrap.contextMenu', 'chart.js']);

module.run(function(editableOptions) {
    editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

function createLevel(n) {
    return {
        description: "New level...",
        points: n || 0
    }
}

function createCharacteristic() {
    return {
        description: "New characteristic...",
        weight: 0,
        levels: [
            createLevel(0),
            createLevel(1),
            createLevel(2)
        ]
    };
}

function createTask(ordering) {
    return {
        description: "New task...",
        ordering:  ordering,
        characteristics: [
            createCharacteristic()
        ]
    }
}

module.controller('StatisticsControl', function($scope, $http, $q) {
    var levels = {};

    $scope.addTask = function() {
        var ordering = $scope.assignment.tasks.map(function(task){
            return task.ordering
        }).reduce(function(a,b) {
            return Math.max(a,b)}, 0
        ) + 1
        $scope.assignment.tasks.push(createTask(ordering))
    };

    $scope.contextMenuForAssignment = function() {
        return [
            ['Add Task', $scope.addTask]
        ]
    };

    $scope.contextMenuForTask = function(task) {
        return $scope.contextMenuForAssignment().concat([
            ['Remove Task', function() {
                $scope.assignment.tasks.splice($scope.assignment.tasks.indexOf(task), 1);
            }],
            null,
            ['Add Characteristic', function() {
                task.characteristics.push(createCharacteristic());
            }]
        ])
    };

    $scope.contextMenuForCharacteristic = function(task, characteristic) {
        return $scope.contextMenuForTask(task).concat([
            ['Remove Characteristic', function() {
                task.characteristics.splice(task.characteristics.indexOf(characteristic), 1);
            }],
            null,
            ['Add Level', function() {
                characteristic.levels.push(createLevel());
            }]
        ])
    };


    $q.all([
        $http.get('json').then(function(res) { return res.data; }),
        $http.get('last-deliveries/json').then(function(res) { return res.data; })
    ]).then(function(res) {
        $scope.assignment = res[0];
        $scope.deliveries = res[1];
    });

    $scope.submit = function submit(assignment) {
        if (assignment) {
            $http.put('json', assignment).then(function(res) {
                $scope.assignment = res.data;
            });
        }
    };

    $scope.$watch('assignment', calculateCountAndCorrel, true);
    $scope.$watch('assignment', $scope.submit, true);

    function calculateCountAndCorrel(assignment) {
        if (!assignment) return;

        assignment.totalAchievablePoints = 0;

        $scope.assignment.tasks.forEach(function(task) {
            task.totalWeight = 0;
            task.totalAchievablePoints = 0;
            task.totalAchievablePointsWithWeight = 0;

            task.characteristics.forEach(function(characteristic) {
                task.totalWeight += characteristic.weight;
                characteristic.achievablePoints = 0;
                characteristic.achievablePointsWithWeight = 0;

                characteristic.levels.forEach(function(level) {
                    level.count = 0;
                    levels[level.id] = level;
                    characteristic.achievablePoints = Math.max(0, Math.max(characteristic.achievablePoints, level.points));

                    Object.defineProperty(level, 'characteristic', {
                        value: characteristic,
                        enumerable: false,
                        configurable: false
                    });
                });

                task.totalAchievablePoints += characteristic.achievablePoints;
                task.totalAchievablePointsWithWeight += characteristic.achievablePoints * characteristic.weight;
            })

            assignment.totalAchievablePoints += task.totalAchievablePointsWithWeight;
        });

        var numPointsToDeliveries = { 1:0, 2:0, 3:0, 4:0, 5:0, 6:0, 7:0, 8:0, 9:0, 10: 0 };

        $scope.deliveries.forEach(function(delivery) {
            // Defaulting to null, so the value is null when a group is not graded (delivery.masteries.length == 0)
            delivery.achievedNumberOfPoints = null;
            delivery.masteries.forEach(function(mastery) {
                mastery = levels[mastery.id];
                mastery.count++;
                delivery.achievedNumberOfPoints += mastery.points * mastery.characteristic.weight;
            })

            if (delivery.achievedNumberOfPoints != null) {
                var name = Math.max(1, Math.round(delivery.achievedNumberOfPoints / assignment.totalAchievablePoints * 10));
                numPointsToDeliveries[name] = numPointsToDeliveries[name] + 1;
            }
        });

        $scope.labels = Object.keys(numPointsToDeliveries)
            .sort(function(a,b) { return a - b; });

        $scope.data = [$scope.labels
            .map(function(name) { return numPointsToDeliveries[name]; })];

        // Construct an array of all actual achieved points per delivery
        // [ 60, 72, 70, 60]
        var achievedNumberOfPoints = $scope.deliveries.map(function(delivery) {
            return delivery.achievedNumberOfPoints;
        });

        $scope.mean = (jStat.mean(achievedNumberOfPoints) / assignment.totalAchievablePoints * 10).toFixed(2);
        $scope.median = (jStat.median(achievedNumberOfPoints) / assignment.totalAchievablePoints * 10).toFixed(2);

        $scope.assignment.tasks.forEach(function(task) {
            task.characteristics.forEach(function (characteristic) {
                // Construct an array of all points for this particular characteristic
                // [ 0, 1, 3, 1, 1]
                var scoresForCharacteristic = $scope.deliveries.map(function(delivery) {
                    var mastery = delivery.masteries.find(function(mastery) {
                        return levels[mastery.id].characteristic === characteristic
                    });
                    return mastery ? mastery.points : null;
                });

                // Compute correlation
                characteristic.correlation =
                    (jStat.corrcoeff(scoresForCharacteristic, achievedNumberOfPoints)).toFixed(2);
                if (isNaN(characteristic.correlation)) {
                    console.warn('Failed to compute correlation for %o and %o', scoresForCharacteristic, achievedNumberOfPoints)
                }
            });
        });
    }
});