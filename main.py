from collections import defaultdict
import numpy as np
import scipy as sp


class Vehicle:
  def __init__(self, departure_time: int, length: int, type: int):
    self.allowed_on_track = set()
    self.departure_time = departure_time
    self.length = length
    self.type = type


class VehicleInstance:
  def __init__(self, vehicle, assigned_track):
    self.vehicle = vehicle
    self.assigned_track = assigned_track


class Track:
  def __init__(self, length: int):
    self.length = length
    self.blocks_track = set()
    self.blocked_by_track = set()
    self.allowed_types = set()


class TrackInstance:
  def __init__(self, track: Track):
    self.track = track
    self.taken_spots = np.array([False for _ in range(track.length)])

  def can_take(self, position: int, vehicle: Vehicle):
    return position + vehicle.length < len(self.taken_spots) \
      and not any(self.taken_spots[position:position + vehicle.length]) \
      and vehicle.type in self.track.allowed_types

  def take(self, position: int, vehicle: Vehicle):
    if not self.can_take(position, vehicle):
      raise Exception('Cannot fit vehicle into position')
    self.taken_spots[position:position + vehicle.length] = True


class Instance:
  def __init__(self, vehicles: list, tracks: list):
    self.vehicles = vehicles
    self.tracks = tracks

    self.type_to_vehicles = {v.type:v for v in vehicles}
    self.type_to_tracks = defaultdict(list)
    for t in tracks:
      for allowed_t in tracks.allowed_types:
        self.type_to_tracks[allowed_t].append(t)

    self.assigned_vehicles = {}
    self.assigned_tracks = {}

  def assign(self, v: Vehicle, t: Track, position: int):
    if v in self.assigned_vehicles:
      raise Exception('Vehicle already assigned')
    self.assigned_vehicles[v] = VehicleInstance(v, t)

    trackinstance = self.assigned_tracks.get(t, TrackInstance(t))
    trackinstance.take(position, v)
    self.assigned_tracks[t] = trackinstance



def load_problem():
  nvehicles = int(input())
  ntracks = int(input())

  def range_in(n, t=str):
      return [t(input()) for _ in range(n)]

  lens = range_in(nvehicles, int)
  types = range_in(nvehicles, int)
  tracks = [range_in(ntracks, int) for _ in range(nvehicles)]
  tracklens = range_in(ntracks, int)
  departts = range_in(nvehicles, int)
  schedtypes = range_in(nvehicles, int)
  blockeds = range_in(ntracks, int)

  # TODO transfer to classes, return that shit


def brute_force(vehicles, tracks):
  instance = Instance(vehicles, tracks)
  while len(instance.assigned_vehicles) != len(vehicles):
    break


