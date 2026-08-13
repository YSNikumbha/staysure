import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Archive, ArrowDown, ArrowUp, BedDouble, Camera, Pencil, Plus, Save, Star, Trash2, X } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Link, NavLink, useParams } from 'react-router-dom';
import { propertyApi } from '../../api/property.api';
import { FormMessage } from '../../components/FormMessage';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import type {
  Bed,
  Floor,
  FloorInput,
  FloorStatus,
  FurnishingType,
  ImageCategory,
  OwnerEditableBedStatus,
  Room,
  RoomInput,
  RoomStatus,
  SharingType
} from '../../types/property';
import { getApiErrorMessage } from '../../utils/apiError';
import { toAssetUrl } from '../../utils/assets';
import { OwnerShell } from './OwnerShell';

type OwnerPgDetailsPageProps = {
  focus?: 'overview' | 'floors' | 'rooms' | 'beds';
};

type FloorFormState = {
  id?: number;
  name: string;
  floorNumber: string;
  description: string;
  status: Exclude<FloorStatus, 'ARCHIVED'>;
};

type RoomFormState = {
  id?: number;
  floorId: string;
  roomNumber: string;
  roomName: string;
  sharingType: SharingType;
  capacity: string;
  monthlyRent: string;
  securityDeposit: string;
  acAvailable: boolean;
  attachedBathroom: boolean;
  furnishingType: FurnishingType;
  status: Exclude<RoomStatus, 'ARCHIVED'>;
  description: string;
};

type BedFormState = {
  id?: number;
  floorId: string;
  roomId: string;
  bedNumber: string;
  bedLabel: string;
  status: OwnerEditableBedStatus;
};

const emptyFloorForm: FloorFormState = {
  name: '',
  floorNumber: '',
  description: '',
  status: 'ACTIVE'
};

const emptyRoomForm: RoomFormState = {
  floorId: '',
  roomNumber: '',
  roomName: '',
  sharingType: 'SINGLE',
  capacity: '1',
  monthlyRent: '0',
  securityDeposit: '0',
  acAvailable: false,
  attachedBathroom: false,
  furnishingType: 'FULLY_FURNISHED',
  status: 'ACTIVE',
  description: ''
};

const emptyBedForm: BedFormState = {
  floorId: '',
  roomId: '',
  bedNumber: '',
  bedLabel: '',
  status: 'AVAILABLE'
};

const sharingTypes: SharingType[] = ['SINGLE', 'DOUBLE', 'TRIPLE', 'FOUR_SHARING', 'DORMITORY'];
const furnishingTypes: FurnishingType[] = ['UNFURNISHED', 'SEMI_FURNISHED', 'FULLY_FURNISHED'];
const roomStatuses: Array<Exclude<RoomStatus, 'ARCHIVED'>> = ['ACTIVE', 'INACTIVE', 'MAINTENANCE'];
const floorStatuses: Array<Exclude<FloorStatus, 'ARCHIVED'>> = ['ACTIVE', 'INACTIVE'];
const bedStatuses: OwnerEditableBedStatus[] = ['AVAILABLE', 'MAINTENANCE', 'INACTIVE'];
const imageCategories: ImageCategory[] = ['BUILDING', 'ROOM', 'BATHROOM', 'KITCHEN', 'DINING', 'COMMON_AREA', 'EXTERIOR', 'PARKING', 'OTHER'];

export function OwnerPgDetailsPage({ focus = 'overview' }: OwnerPgDetailsPageProps) {
  const { id } = useParams();
  const pgId = Number(id);
  const queryClient = useQueryClient();
  const [actionError, setActionError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [floorForm, setFloorForm] = useState<FloorFormState>(emptyFloorForm);
  const [roomForm, setRoomForm] = useState<RoomFormState>(emptyRoomForm);
  const [bedForm, setBedForm] = useState<BedFormState>(emptyBedForm);
  const [selectedAmenityIds, setSelectedAmenityIds] = useState<Set<number>>(new Set());
  const [imageCategory, setImageCategory] = useState<ImageCategory>('BUILDING');
  const [coverImage, setCoverImage] = useState(false);
  const [imageFile, setImageFile] = useState<File | null>(null);

  const propertyQuery = useQuery({
    queryKey: ['owner-pg', pgId],
    queryFn: () => propertyApi.property(pgId),
    enabled: Number.isFinite(pgId)
  });

  const amenitiesQuery = useQuery({
    queryKey: ['owner-amenities'],
    queryFn: propertyApi.amenities
  });

  const details = propertyQuery.data;
  const floors = details?.floors ?? [];
  const rooms = useMemo(() => floors.flatMap((floor) => floor.rooms), [floors]);
  const roomsForSelectedFloor = useMemo(
    () => floors.find((floor) => String(floor.id) === bedForm.floorId)?.rooms ?? [],
    [bedForm.floorId, floors]
  );

  useEffect(() => {
    setSelectedAmenityIds(new Set(details?.amenities.map((amenity) => amenity.id) ?? []));
  }, [details?.amenities]);

  useEffect(() => {
    const firstFloor = floors[0];
    if (firstFloor && !roomForm.floorId) {
      setRoomForm((current) => ({ ...current, floorId: String(firstFloor.id) }));
    }
    if (firstFloor && !bedForm.floorId) {
      const firstRoom = firstFloor.rooms[0];
      setBedForm((current) => ({
        ...current,
        floorId: String(firstFloor.id),
        roomId: firstRoom ? String(firstRoom.id) : ''
      }));
    }
  }, [bedForm.floorId, floors, roomForm.floorId]);

  useEffect(() => {
    if (roomsForSelectedFloor.length > 0 && !roomsForSelectedFloor.some((room) => String(room.id) === bedForm.roomId)) {
      setBedForm((current) => ({ ...current, roomId: String(roomsForSelectedFloor[0].id) }));
    }
    if (roomsForSelectedFloor.length === 0 && bedForm.roomId) {
      setBedForm((current) => ({ ...current, roomId: '' }));
    }
  }, [bedForm.roomId, roomsForSelectedFloor]);

  const invalidate = async () => {
    await queryClient.invalidateQueries({ queryKey: ['owner-pg', pgId] });
    await queryClient.invalidateQueries({ queryKey: ['owner-pgs'] });
    await queryClient.invalidateQueries({ queryKey: ['owner-dashboard'] });
  };

  const saveFloorMutation = useMutation({
    mutationFn: (input: { id?: number; data: FloorInput }) => input.id
      ? propertyApi.updateFloor(pgId, input.id, input.data)
      : propertyApi.createFloor(pgId, input.data),
    onSuccess: async () => {
      setFloorForm(emptyFloorForm);
      setActionError(null);
      setSuccessMessage('Floor saved.');
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to save floor'))
  });

  const archiveFloorMutation = useMutation({
    mutationFn: (floorId: number) => propertyApi.archiveFloor(pgId, floorId),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to archive floor'))
  });

  const saveRoomMutation = useMutation({
    mutationFn: (input: { floorId: number; id?: number; data: RoomInput }) => input.id
      ? propertyApi.updateRoom(pgId, input.floorId, input.id, input.data)
      : propertyApi.createRoom(pgId, input.floorId, input.data),
    onSuccess: async () => {
      setRoomForm((current) => ({ ...emptyRoomForm, floorId: current.floorId }));
      setActionError(null);
      setSuccessMessage('Room saved.');
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to save room'))
  });

  const archiveRoomMutation = useMutation({
    mutationFn: (input: { floorId: number; roomId: number }) => propertyApi.archiveRoom(pgId, input.floorId, input.roomId),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to archive room'))
  });

  const saveBedMutation = useMutation({
    mutationFn: (input: { floorId: number; roomId: number; id?: number; data: BedFormState }) => {
      const payload = {
        bedNumber: input.data.bedNumber.trim(),
        bedLabel: blankToUndefined(input.data.bedLabel),
        status: input.data.status
      };
      return input.id
        ? propertyApi.updateBed(pgId, input.floorId, input.roomId, input.id, payload)
        : propertyApi.createBed(pgId, input.floorId, input.roomId, payload);
    },
    onSuccess: async () => {
      setBedForm((current) => ({ ...emptyBedForm, floorId: current.floorId, roomId: current.roomId }));
      setActionError(null);
      setSuccessMessage('Bed saved.');
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to save bed'))
  });

  const archiveBedMutation = useMutation({
    mutationFn: (input: { floorId: number; roomId: number; bedId: number }) => propertyApi.archiveBed(pgId, input.floorId, input.roomId, input.bedId),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to archive bed'))
  });

  const saveAmenitiesMutation = useMutation({
    mutationFn: () => propertyApi.updateAmenities(pgId, [...selectedAmenityIds]),
    onSuccess: async () => {
      setActionError(null);
      setSuccessMessage('Amenities updated.');
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to update amenities'))
  });

  const uploadImageMutation = useMutation({
    mutationFn: () => {
      if (!imageFile) {
        throw new Error('Choose an image file');
      }
      return propertyApi.uploadImage({ pgId, category: imageCategory, coverImage, file: imageFile });
    },
    onSuccess: async () => {
      setImageFile(null);
      setCoverImage(false);
      setActionError(null);
      setSuccessMessage('Image uploaded.');
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to upload image'))
  });

  const setCoverMutation = useMutation({
    mutationFn: (imageId: number) => propertyApi.setCoverImage(pgId, imageId),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to set cover image'))
  });

  const deleteImageMutation = useMutation({
    mutationFn: (imageId: number) => propertyApi.deleteImage(pgId, imageId),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to delete image'))
  });

  const reorderImagesMutation = useMutation({
    mutationFn: (order: Array<{ imageId: number; sortOrder: number }>) => propertyApi.reorderImages(pgId, order),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to reorder images'))
  });

  const submitVerificationMutation = useMutation({
    mutationFn: () => propertyApi.submitVerification(pgId),
    onSuccess: async () => {
      setActionError(null);
      setSuccessMessage('PG submitted for verification.');
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to submit PG for verification'))
  });

  if (propertyQuery.isLoading) {
    return <div className="route-state">Loading</div>;
  }

  if (propertyQuery.isError || !details) {
    return <div className="route-state">{getApiErrorMessage(propertyQuery.error, 'Unable to load PG')}</div>;
  }

  const property = details.property;

  const saveFloor = () => {
    if (!floorForm.name.trim() || !floorForm.floorNumber.trim()) {
      setActionError('Floor name and number are required.');
      return;
    }
    saveFloorMutation.mutate({
      id: floorForm.id,
      data: {
        name: floorForm.name.trim(),
        floorNumber: Number(floorForm.floorNumber),
        description: blankToUndefined(floorForm.description),
        status: floorForm.status
      }
    });
  };

  const saveRoom = () => {
    if (!roomForm.floorId || !roomForm.roomNumber.trim()) {
      setActionError('Floor and room number are required.');
      return;
    }
    saveRoomMutation.mutate({
      floorId: Number(roomForm.floorId),
      id: roomForm.id,
      data: {
        roomNumber: roomForm.roomNumber.trim(),
        roomName: blankToUndefined(roomForm.roomName),
        sharingType: roomForm.sharingType,
        capacity: Number(roomForm.capacity),
        monthlyRent: Number(roomForm.monthlyRent),
        securityDeposit: Number(roomForm.securityDeposit),
        acAvailable: roomForm.acAvailable,
        attachedBathroom: roomForm.attachedBathroom,
        furnishingType: roomForm.furnishingType,
        status: roomForm.status,
        description: blankToUndefined(roomForm.description)
      }
    });
  };

  const saveBed = () => {
    if (!bedForm.floorId || !bedForm.roomId || !bedForm.bedNumber.trim()) {
      setActionError('Floor, room, and bed number are required.');
      return;
    }
    saveBedMutation.mutate({
      floorId: Number(bedForm.floorId),
      roomId: Number(bedForm.roomId),
      id: bedForm.id,
      data: bedForm
    });
  };

  const moveImage = (imageId: number, direction: -1 | 1) => {
    const current = [...details.images];
    const index = current.findIndex((image) => image.id === imageId);
    const target = index + direction;
    if (index < 0 || target < 0 || target >= current.length) {
      return;
    }
    [current[index], current[target]] = [current[target], current[index]];
    reorderImagesMutation.mutate(current.map((image, sortOrder) => ({ imageId: image.id, sortOrder })));
  };

  return (
    <OwnerShell>
      <div className="stack">
        <PageHeader
          eyebrow="PG management"
          title={property.name}
          actions={
            <div className="action-row">
              <Link className="secondary-link" to="/owner/pgs">Back</Link>
              <Link className="primary-link" to={`/owner/pgs/${pgId}/edit`}>Edit PG</Link>
              <button
                className="secondary-button"
                type="button"
                onClick={() => submitVerificationMutation.mutate()}
                disabled={submitVerificationMutation.isPending || property.verificationStatus === 'PENDING' || property.verificationStatus === 'UNDER_REVIEW' || property.verificationStatus === 'VERIFIED'}
              >
                Submit Verification
              </button>
            </div>
          }
        />

        <div className="segmented">
          <NavLink end to={`/owner/pgs/${pgId}`}>Overview</NavLink>
          <NavLink to={`/owner/pgs/${pgId}/floors`}>Floors</NavLink>
          <NavLink to={`/owner/pgs/${pgId}/rooms`}>Rooms</NavLink>
          <NavLink to={`/owner/pgs/${pgId}/beds`}>Beds</NavLink>
        </div>

        <section className="surface status-surface">
          <div>
            <h2>{property.area}, {property.city}</h2>
            <p>{property.addressLine1} · {property.genderType.replaceAll('_', ' ')} · {property.propertyType.replaceAll('_', ' ')}</p>
            {property.verificationRemarks || property.rejectionReason ? <p>{property.rejectionReason || property.verificationRemarks}</p> : null}
          </div>
          <div className="badge-row">
            <StatusBadge status={property.status} />
            <StatusBadge status={property.verificationStatus} />
          </div>
        </section>

        <section className="owner-metric-grid">
          <div className="metric-tile">
            <span>Total Floors</span>
            <strong>{details.counts.totalFloors}</strong>
          </div>
          <div className="metric-tile">
            <span>Total Rooms</span>
            <strong>{details.counts.totalRooms}</strong>
          </div>
          <div className="metric-tile">
            <span>Total Beds</span>
            <strong>{details.counts.totalBeds}</strong>
          </div>
          <div className="metric-tile">
            <span>Available Beds</span>
            <strong>{details.counts.availableBeds}</strong>
          </div>
          <div className="metric-tile">
            <span>Maintenance Beds</span>
            <strong>{details.counts.maintenanceBeds}</strong>
          </div>
        </section>

        <FormMessage message={actionError} />
        <FormMessage message={successMessage} tone="success" />

        {focus === 'overview' ? (
          <section className="surface detail-grid">
            <div>
              <span>Starting rent</span>
              <strong>Rs {Number(property.startingRent).toLocaleString()}</strong>
            </div>
            <div>
              <span>Security deposit</span>
              <strong>Rs {Number(property.securityDeposit).toLocaleString()}</strong>
            </div>
            <div>
              <span>Notice period</span>
              <strong>{property.noticePeriodDays} days</strong>
            </div>
            <div>
              <span>Lock-in</span>
              <strong>{property.lockInMonths} months</strong>
            </div>
          </section>
        ) : null}

        <section className="surface management-section" id="floors">
          <div className="section-heading">
            <h2>Floors</h2>
            {floorForm.id ? (
              <button className="secondary-button compact-button" type="button" onClick={() => setFloorForm(emptyFloorForm)}>
                <X size={16} />
                Cancel
              </button>
            ) : null}
          </div>
          <div className="inline-form">
            <label>
              Name
              <input value={floorForm.name} onChange={(event) => setFloorForm((current) => ({ ...current, name: event.target.value }))} />
            </label>
            <label>
              Floor Number
              <input type="number" value={floorForm.floorNumber} onChange={(event) => setFloorForm((current) => ({ ...current, floorNumber: event.target.value }))} />
            </label>
            <label>
              Status
              <select value={floorForm.status} onChange={(event) => setFloorForm((current) => ({ ...current, status: event.target.value as Exclude<FloorStatus, 'ARCHIVED'> }))}>
                {floorStatuses.map((status) => <option key={status} value={status}>{status}</option>)}
              </select>
            </label>
            <label className="form-span">
              Description
              <input value={floorForm.description} onChange={(event) => setFloorForm((current) => ({ ...current, description: event.target.value }))} />
            </label>
            <button className="primary-button" type="button" onClick={saveFloor} disabled={saveFloorMutation.isPending}>
              {floorForm.id ? <Save size={17} /> : <Plus size={17} />}
              {floorForm.id ? 'Update Floor' : 'Add Floor'}
            </button>
          </div>
        </section>

        <section className="surface management-section" id="rooms">
          <div className="section-heading">
            <h2>Rooms</h2>
            {roomForm.id ? (
              <button className="secondary-button compact-button" type="button" onClick={() => setRoomForm((current) => ({ ...emptyRoomForm, floorId: current.floorId }))}>
                <X size={16} />
                Cancel
              </button>
            ) : null}
          </div>
          <div className="inline-form three-column">
            <label>
              Floor
              <select
                value={roomForm.floorId}
                disabled={Boolean(roomForm.id)}
                onChange={(event) => setRoomForm((current) => ({ ...current, floorId: event.target.value }))}
              >
                <option value="">Select floor</option>
                {floors.map((floor) => <option key={floor.id} value={floor.id}>{floor.name}</option>)}
              </select>
            </label>
            <label>
              Room Number
              <input value={roomForm.roomNumber} onChange={(event) => setRoomForm((current) => ({ ...current, roomNumber: event.target.value }))} />
            </label>
            <label>
              Room Name
              <input value={roomForm.roomName} onChange={(event) => setRoomForm((current) => ({ ...current, roomName: event.target.value }))} />
            </label>
            <label>
              Sharing Type
              <select value={roomForm.sharingType} onChange={(event) => setRoomForm((current) => ({ ...current, sharingType: event.target.value as SharingType }))}>
                {sharingTypes.map((type) => <option key={type} value={type}>{type.replaceAll('_', ' ')}</option>)}
              </select>
            </label>
            <label>
              Capacity
              <input type="number" min="1" value={roomForm.capacity} onChange={(event) => setRoomForm((current) => ({ ...current, capacity: event.target.value }))} />
            </label>
            <label>
              Rent
              <input type="number" min="0" value={roomForm.monthlyRent} onChange={(event) => setRoomForm((current) => ({ ...current, monthlyRent: event.target.value }))} />
            </label>
            <label>
              Security Deposit
              <input type="number" min="0" value={roomForm.securityDeposit} onChange={(event) => setRoomForm((current) => ({ ...current, securityDeposit: event.target.value }))} />
            </label>
            <label>
              Furnishing
              <select value={roomForm.furnishingType} onChange={(event) => setRoomForm((current) => ({ ...current, furnishingType: event.target.value as FurnishingType }))}>
                {furnishingTypes.map((type) => <option key={type} value={type}>{type.replaceAll('_', ' ')}</option>)}
              </select>
            </label>
            <label>
              Status
              <select value={roomForm.status} onChange={(event) => setRoomForm((current) => ({ ...current, status: event.target.value as Exclude<RoomStatus, 'ARCHIVED'> }))}>
                {roomStatuses.map((status) => <option key={status} value={status}>{status}</option>)}
              </select>
            </label>
            <label className="checkbox-field">
              <input type="checkbox" checked={roomForm.acAvailable} onChange={(event) => setRoomForm((current) => ({ ...current, acAvailable: event.target.checked }))} />
              AC
            </label>
            <label className="checkbox-field">
              <input type="checkbox" checked={roomForm.attachedBathroom} onChange={(event) => setRoomForm((current) => ({ ...current, attachedBathroom: event.target.checked }))} />
              Bathroom
            </label>
            <label className="form-span">
              Description
              <input value={roomForm.description} onChange={(event) => setRoomForm((current) => ({ ...current, description: event.target.value }))} />
            </label>
            <button className="primary-button" type="button" onClick={saveRoom} disabled={saveRoomMutation.isPending || floors.length === 0}>
              {roomForm.id ? <Save size={17} /> : <Plus size={17} />}
              {roomForm.id ? 'Update Room' : 'Add Room'}
            </button>
          </div>
        </section>

        <section className="surface management-section" id="beds">
          <div className="section-heading">
            <h2>Beds</h2>
            {bedForm.id ? (
              <button className="secondary-button compact-button" type="button" onClick={() => setBedForm((current) => ({ ...emptyBedForm, floorId: current.floorId, roomId: current.roomId }))}>
                <X size={16} />
                Cancel
              </button>
            ) : null}
          </div>
          <div className="inline-form three-column">
            <label>
              Floor
              <select
                value={bedForm.floorId}
                disabled={Boolean(bedForm.id)}
                onChange={(event) => setBedForm((current) => ({ ...current, floorId: event.target.value, roomId: '' }))}
              >
                <option value="">Select floor</option>
                {floors.map((floor) => <option key={floor.id} value={floor.id}>{floor.name}</option>)}
              </select>
            </label>
            <label>
              Room
              <select
                value={bedForm.roomId}
                disabled={Boolean(bedForm.id)}
                onChange={(event) => setBedForm((current) => ({ ...current, roomId: event.target.value }))}
              >
                <option value="">Select room</option>
                {roomsForSelectedFloor.map((room) => (
                  <option key={room.id} value={room.id}>
                    {room.roomNumber} ({room.bedCount}/{room.capacity})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Bed Number
              <input value={bedForm.bedNumber} onChange={(event) => setBedForm((current) => ({ ...current, bedNumber: event.target.value }))} />
            </label>
            <label>
              Bed Label
              <input value={bedForm.bedLabel} onChange={(event) => setBedForm((current) => ({ ...current, bedLabel: event.target.value }))} />
            </label>
            <label>
              Status
              <select value={bedForm.status} onChange={(event) => setBedForm((current) => ({ ...current, status: event.target.value as OwnerEditableBedStatus }))}>
                {bedStatuses.map((status) => <option key={status} value={status}>{status}</option>)}
              </select>
            </label>
            <button className="primary-button" type="button" onClick={saveBed} disabled={saveBedMutation.isPending || rooms.length === 0}>
              {bedForm.id ? <Save size={17} /> : <Plus size={17} />}
              {bedForm.id ? 'Update Bed' : 'Add Bed'}
            </button>
          </div>
        </section>

        <section className="surface management-section">
          <div className="section-heading">
            <h2>Amenities</h2>
            <button className="primary-button compact-button" type="button" onClick={() => saveAmenitiesMutation.mutate()} disabled={saveAmenitiesMutation.isPending}>
              <Save size={16} />
              Save
            </button>
          </div>
          <div className="amenity-grid">
            {(amenitiesQuery.data ?? []).map((amenity) => (
              <label className="amenity-option" key={amenity.id}>
                <input
                  type="checkbox"
                  checked={selectedAmenityIds.has(amenity.id)}
                  onChange={(event) => {
                    setSelectedAmenityIds((current) => {
                      const next = new Set(current);
                      if (event.target.checked) {
                        next.add(amenity.id);
                      } else {
                        next.delete(amenity.id);
                      }
                      return next;
                    });
                  }}
                />
                <span>{amenity.name}</span>
              </label>
            ))}
            {amenitiesQuery.isLoading ? <p>Loading amenities</p> : null}
          </div>
        </section>

        <section className="surface management-section">
          <div className="section-heading">
            <h2>Gallery</h2>
            <Camera size={20} />
          </div>
          <div className="document-upload gallery-upload">
            <label>
              Category
              <select value={imageCategory} onChange={(event) => setImageCategory(event.target.value as ImageCategory)}>
                {imageCategories.map((category) => <option key={category} value={category}>{category.replaceAll('_', ' ')}</option>)}
              </select>
            </label>
            <label>
              Image
              <input type="file" accept="image/png,image/jpeg" onChange={(event) => setImageFile(event.target.files?.[0] ?? null)} />
            </label>
            <label className="checkbox-field">
              <input type="checkbox" checked={coverImage} onChange={(event) => setCoverImage(event.target.checked)} />
              Cover Image
            </label>
            <button className="primary-button" type="button" onClick={() => uploadImageMutation.mutate()} disabled={uploadImageMutation.isPending}>
              <Plus size={17} />
              Upload
            </button>
          </div>
          <div className="gallery-grid">
            {details.images.map((image, index) => (
              <article className="gallery-item" key={image.id}>
                <img src={toAssetUrl(image.imageUrl)} alt={image.category.replaceAll('_', ' ')} />
                <div className="gallery-meta">
                  <span>{image.category.replaceAll('_', ' ')}</span>
                  {image.coverImage ? <StatusBadge status="COVER" /> : null}
                </div>
                <div className="action-row">
                  <button className="icon-button" type="button" title="Move up" aria-label="Move image up" onClick={() => moveImage(image.id, -1)} disabled={index === 0}>
                    <ArrowUp size={15} />
                  </button>
                  <button className="icon-button" type="button" title="Move down" aria-label="Move image down" onClick={() => moveImage(image.id, 1)} disabled={index === details.images.length - 1}>
                    <ArrowDown size={15} />
                  </button>
                  <button className="icon-button" type="button" title="Set cover" aria-label="Set cover image" onClick={() => setCoverMutation.mutate(image.id)} disabled={image.coverImage}>
                    <Star size={15} />
                  </button>
                  <button className="icon-button" type="button" title="Delete" aria-label="Delete image" onClick={() => deleteImageMutation.mutate(image.id)}>
                    <Trash2 size={15} />
                  </button>
                </div>
              </article>
            ))}
            {details.images.length === 0 ? <p>No images uploaded.</p> : null}
          </div>
        </section>

        <section className="surface management-section">
          <h2>Property Rules</h2>
          <div className="rule-grid">
            <RuleItem label="Visitors" enabled={details.rules?.visitorAllowed ?? false} />
            <RuleItem label="Smoking" enabled={details.rules?.smokingAllowed ?? false} />
            <RuleItem label="Alcohol" enabled={details.rules?.alcoholAllowed ?? false} />
            <RuleItem label="Cooking" enabled={details.rules?.cookingAllowed ?? false} />
            <RuleItem label="Late Entry" enabled={details.rules?.lateEntryAllowed ?? false} />
            <div>
              <span>Gate closing</span>
              <strong>{details.rules?.gateClosingTime ? details.rules.gateClosingTime.slice(0, 5) : 'Not set'}</strong>
            </div>
            <div className="form-span">
              <span>Additional rules</span>
              <strong>{details.rules?.additionalRules || 'None'}</strong>
            </div>
          </div>
        </section>

        <section className="surface management-section">
          <h2>Complete PG Structure</h2>
          <div className="inventory-tree">
            {floors.map((floor) => (
              <div className="tree-floor" key={floor.id}>
                <div className="tree-row tree-row--floor">
                  <div>
                    <strong>{floor.name}</strong>
                    <span>Floor {floor.floorNumber} · {floor.roomCount} rooms · {floor.bedCount} beds</span>
                  </div>
                  <div className="action-row">
                    <StatusBadge status={floor.status} />
                    <button className="icon-button" type="button" title="Edit floor" aria-label="Edit floor" onClick={() => editFloor(floor, setFloorForm)}>
                      <Pencil size={15} />
                    </button>
                    <button className="icon-button" type="button" title="Archive floor" aria-label="Archive floor" onClick={() => confirmArchiveFloor(floor, archiveFloorMutation.mutate)}>
                      <Archive size={15} />
                    </button>
                  </div>
                </div>
                {floor.rooms.map((room) => (
                  <div className="tree-room" key={room.id}>
                    <div className="tree-row">
                      <div>
                        <strong>Room {room.roomNumber}</strong>
                        <span>{room.sharingType.replaceAll('_', ' ')} · {room.bedCount}/{room.capacity} beds · Rs {Number(room.monthlyRent).toLocaleString()}</span>
                      </div>
                      <div className="action-row">
                        <StatusBadge status={room.status} />
                        <button className="icon-button" type="button" title="Edit room" aria-label="Edit room" onClick={() => editRoom(room, setRoomForm)}>
                          <Pencil size={15} />
                        </button>
                        <button className="icon-button" type="button" title="Archive room" aria-label="Archive room" onClick={() => confirmArchiveRoom(room, archiveRoomMutation.mutate)}>
                          <Archive size={15} />
                        </button>
                      </div>
                    </div>
                    <div className="bed-list">
                      {room.beds.map((bed) => (
                        <div className="bed-chip" key={bed.id}>
                          <BedDouble size={15} />
                          <span>{bed.bedLabel || `Bed ${bed.bedNumber}`}</span>
                          <StatusBadge status={bed.status} />
                          <button className="icon-button" type="button" title="Edit bed" aria-label="Edit bed" onClick={() => editBed(floor.id, bed, setBedForm)}>
                            <Pencil size={14} />
                          </button>
                          <button className="icon-button" type="button" title="Archive bed" aria-label="Archive bed" onClick={() => confirmArchiveBed(floor.id, bed, archiveBedMutation.mutate)}>
                            <Archive size={14} />
                          </button>
                        </div>
                      ))}
                      {room.beds.length === 0 ? <p>No beds added.</p> : null}
                    </div>
                  </div>
                ))}
                {floor.rooms.length === 0 ? <p>No rooms added on this floor.</p> : null}
              </div>
            ))}
            {floors.length === 0 ? <p>No floors added.</p> : null}
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}

function RuleItem({ label, enabled }: { label: string; enabled: boolean }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{enabled ? 'Allowed' : 'Not allowed'}</strong>
    </div>
  );
}

function editFloor(floor: Floor, setFloorForm: (value: FloorFormState) => void) {
  setFloorForm({
    id: floor.id,
    name: floor.name,
    floorNumber: String(floor.floorNumber),
    description: floor.description ?? '',
    status: floor.status === 'ARCHIVED' ? 'INACTIVE' : floor.status
  });
}

function editRoom(room: Room, setRoomForm: (value: RoomFormState) => void) {
  setRoomForm({
    id: room.id,
    floorId: String(room.floorId),
    roomNumber: room.roomNumber,
    roomName: room.roomName ?? '',
    sharingType: room.sharingType,
    capacity: String(room.capacity),
    monthlyRent: String(room.monthlyRent),
    securityDeposit: String(room.securityDeposit),
    acAvailable: room.acAvailable,
    attachedBathroom: room.attachedBathroom,
    furnishingType: room.furnishingType,
    status: room.status === 'ARCHIVED' ? 'INACTIVE' : room.status,
    description: room.description ?? ''
  });
}

function editBed(floorId: number, bed: Bed, setBedForm: (value: BedFormState) => void) {
  setBedForm({
    id: bed.id,
    floorId: String(floorId),
    roomId: String(bed.roomId),
    bedNumber: bed.bedNumber,
    bedLabel: bed.bedLabel ?? '',
    status: bed.status === 'AVAILABLE' || bed.status === 'MAINTENANCE' || bed.status === 'INACTIVE' ? bed.status : 'AVAILABLE'
  });
}

function confirmArchiveFloor(floor: Floor, archive: (floorId: number) => void) {
  if (window.confirm(`Archive ${floor.name}?`)) {
    archive(floor.id);
  }
}

function confirmArchiveRoom(room: Room, archive: (input: { floorId: number; roomId: number }) => void) {
  if (window.confirm(`Archive room ${room.roomNumber}?`)) {
    archive({ floorId: room.floorId, roomId: room.id });
  }
}

function confirmArchiveBed(floorId: number, bed: Bed, archive: (input: { floorId: number; roomId: number; bedId: number }) => void) {
  if (window.confirm(`Archive bed ${bed.bedLabel || bed.bedNumber}?`)) {
    archive({ floorId, roomId: bed.roomId, bedId: bed.id });
  }
}

function blankToUndefined(value?: string) {
  return value && value.trim() ? value.trim() : undefined;
}
